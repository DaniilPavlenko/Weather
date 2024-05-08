package ru.dpav.weather.feature.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PermissionResult
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.IconOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import ru.dpav.weather.R
import ru.dpav.weather.api.model.City
import ru.dpav.weather.databinding.FragmentMapBinding
import ru.dpav.weather.domain.model.GeoCoordinate
import ru.dpav.weather.feature.cities_list.ListFragment
import ru.dpav.weather.feature.city_details.CityDetailFragment
import ru.dpav.weather.ui.GoogleApiAvailabilityChecker

class MapFragment : Fragment(R.layout.fragment_map) {

    private val viewModel: MapViewModel by viewModels()

    private var binding: FragmentMapBinding? = null
    private var mapView: MapView? = null

    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private val locationUpdatesCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(location: LocationResult) {
                // FIXME: infinite updating state
                //  How to reproduce:
                //  1. Request the user's location
                //  2. Rotate the device once while the UI is in the updating state
                //  Potential solution: reset `UiState.isLocationUpdating`
                //  when location request canceled or view is destroyed.
                viewModel.onLocationUpdateCompleted()
                val map = checkNotNull(mapView) { "MapView is null" }

                val lastLocation = location.lastLocation ?: return
                val point = GeoPoint(lastLocation.latitude, lastLocation.longitude)

                viewModel.onRequestWeatherAt(point.toGeoCoordinate())

                setMapMarker(point, isUserLocation = true)
                moveCameraTo(point, map.zoomLevelDouble)
            }
        }
    }

    private var isInfoWindowOpened: Boolean = false
    private var markers: ArrayList<Marker> = arrayListOf()

    private val permissionsRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.getOrDefault(PERMISSION_LOCATION, false)) {
            onLocationPermissionGranted()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = "weather-app"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(FragmentMapBinding.bind(view)) {
            binding = this

            btnShowListOfCities.setOnClickListener { navigateToList() }
            btnRequestLocation.setOnClickListener { onClickRequestLocation() }

            mapView = map
            initMap(map)
        }

        val mapCenter = viewModel.mapCenter.value.toGeoPoint()
        val zoom = viewModel.mapZoomLevel.value

        moveCameraTo(mapCenter, zoom, animationSpeed = 0)
        val markerPosition = viewModel.uiState.value.requestedPosition?.toGeoPoint() ?: mapCenter
        setMapMarker(markerPosition)

        viewModel.onRequestWeatherAt(markerPosition.toGeoCoordinate())

        val activity = requireActivity()
        if (GoogleApiAvailabilityChecker.isAvailable(activity)) {
            fusedLocationProvider = LocationServices.getFusedLocationProviderClient(activity)
            viewModel.onLocationServiceAvailable()
        } else {
            viewModel.onLocationServiceUnavailable()
        }

        viewModel.uiState.onEach { uiState ->
            updateCitiesMarkers(uiState.cities)
            binding?.run {
                mapUpdateLayout.isVisible = uiState.isLoading || uiState.isLocationUpdating
                btnShowListOfCities.isVisible = uiState.cities.isNotEmpty()
                btnRequestLocation.isEnabled = uiState.isLocationServicesAvailable
            }
            if (uiState.hasConnectionError) {
                showConnectionError()
            }
            if (uiState.showTurnedOffGeoInfo) {
                showSnack(R.string.geo_disabled, viewModel::onShownDisabledGeoInfo)
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)


        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) mapView?.onResume()
            if (event == Lifecycle.Event.ON_PAUSE) mapView?.onPause()
            if (event == Lifecycle.Event.ON_DESTROY) {
                if (::fusedLocationProvider.isInitialized) {
                    fusedLocationProvider.removeLocationUpdates(locationUpdatesCallback)
                }
                binding = null
                mapView = null
            }
        })
    }

    private fun initMap(map: MapView): Unit = with(map) {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        val maxMapLatitude = MapView.getTileSystem().maxLatitude
        setScrollableAreaLimitLatitude(maxMapLatitude, -maxMapLatitude, (height / 2))
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        minZoomLevel = MIN_ZOOM_LEVEL
        controller.setZoom(MapDefaults.ZOOM)
        isVerticalMapRepetitionEnabled = false
        setMapListener(map)
        addEventReceiver(map)
    }

    private fun setMapListener(map: MapView) {
        map.addMapListener(DelayedMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean = setCurrentPosition()
            override fun onZoom(event: ZoomEvent?): Boolean = setCurrentPosition()

            fun setCurrentPosition(): Boolean {
                val zoomLevel = map.zoomLevelDouble
                val mapCenter = map.mapCenter.toDoubleArray()
                viewModel.saveMapState(mapCenter, zoomLevel)
                return true
            }
        }, MAP_LISTENER_DELAY))
    }

    private fun addEventReceiver(map: MapView) {
        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun longPressHelper(point: GeoPoint): Boolean {
                closeInfoWindow()
                return false
            }

            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                if (isInfoWindowOpened) {
                    closeInfoWindow()
                    return false
                }
                viewModel.onRequestWeatherAt(point.toGeoCoordinate())
                setMapMarker(point)
                moveCameraTo(point)
                return true
            }
        })
        map.overlays.add(eventsOverlay)
    }

    private fun setMapMarker(point: GeoPoint, isUserLocation: Boolean = false) {
        val map = checkNotNull(mapView)
        val icon = if (isUserLocation) {
            R.drawable.ic_marker_location
        } else {
            R.drawable.ic_marker_search_final
        }
        val searchIconOverlay = IconOverlay(
            point,
            ContextCompat.getDrawable(requireContext(), icon)
        )
        with(map) {
            if (overlays.lastIndex < OVERLAY_SEARCH_POINT) {
                overlays.add(OVERLAY_SEARCH_POINT, searchIconOverlay)
                return
            }
            overlays[OVERLAY_SEARCH_POINT] = searchIconOverlay
            invalidate()
        }
    }

    private fun updateCitiesMarkers(cities: List<City>) {
        // TODO: try to reduce update count. By checking was the list `cities` changed or not.
        val map = mapView ?: return
        map.overlays.removeAll(markers)
        markers.clear()
        addMarkersOnMap(map, cities, R.drawable.ic_marker_city)
        map.invalidate()
    }

    private fun addMarkersOnMap(map: MapView, cities: List<City>, markersIcon: Int) {
        val citiesMarkers = cities.map { makeMarker(map, it, markersIcon) }
        markers.addAll(citiesMarkers)
        map.overlays.addAll(citiesMarkers)
    }

    private fun makeMarker(mapView: MapView, city: City, icon: Int) = Marker(mapView).apply {
        id = city.id.toString()
        position = GeoPoint(
            city.coordinates.latitude,
            city.coordinates.longitude
        )
        setAnchor(Marker.ANCHOR_CENTER, 1f)
        setIcon(ContextCompat.getDrawable(requireContext(), icon))
        infoWindow = PopInfoWindow(R.layout.info_window_weather, mapView, city) {
            openDetailDialog(city)
        }
        setOnMarkerClickListener { marker, _ ->
            openInfoWindow(marker)
            return@setOnMarkerClickListener true
        }
    }

    private fun openDetailDialog(city: City) {
        val detailFragment = CityDetailFragment.newInstance(city.id)
        parentFragmentManager.commit {
            replace(R.id.mainFragmentContainer, detailFragment, "details")
            addToBackStack("details")
        }
    }

    private fun openInfoWindow(marker: Marker) {
        closeInfoWindow()
        isInfoWindowOpened = true

        marker.showInfoWindow()
        marker.icon = getMarkerIcon(isSelected = true)
    }

    private fun closeInfoWindow() {
        isInfoWindowOpened = false
        val windows = InfoWindow.getOpenedInfoWindowsOn(mapView)
        windows.forEach { window ->
            window.close()
            val relatedObject = window.relatedObject
            if (relatedObject is Marker) {
                relatedObject.icon = getMarkerIcon(isSelected = false)
            }
        }
    }

    private fun moveCameraTo(
        point: GeoPoint,
        zoom: Double? = null,
        animationSpeed: Long = ANIMATION_SPEED,
    ) {
        val map = mapView ?: return
        map.controller?.animateTo(point, zoom ?: map.zoomLevelDouble, animationSpeed)
    }

    private fun onLocationPermissionGranted() {
        if (!isLocationEnabled()) {
            viewModel.onLocationTurnedOff()
            return
        }
        requestLocationUpdate()
    }

    private fun onClickRequestLocation() {
        if (!hasLocationPermission()) {
            askPermission()
            return
        }
        if (!isLocationEnabled()) {
            viewModel.onLocationTurnedOff()
            return
        }
        closeInfoWindow()
        requestLocationUpdate()
    }

    private fun showConnectionError() {
        val view = view ?: return
        Snackbar.make(view, R.string.connection_error, Snackbar.LENGTH_INDEFINITE).apply {
            setAction(R.string.retry) {
                viewModel.onRetryRequest()
                dismiss()
            }
            show()
        }
    }

    private fun navigateToList() {
        parentFragmentManager.commit {
            replace(R.id.mainFragmentContainer, ListFragment.newInstance(), "list")
            addToBackStack("list")
        }
    }

    private fun showSnack(resId: Int, shownCallback: () -> Unit) {
        val view = view ?: return
        Snackbar.make(view, getString(resId), Snackbar.LENGTH_LONG)
            .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    shownCallback()
                }
            })
            .show()
    }

    private fun askPermission() {
        if (!hasLocationPermission()) {
            permissionsRequestLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private fun getMarkerIcon(isSelected: Boolean): Drawable {
        val icon: Int = if (isSelected) {
            R.drawable.ic_marker_city_selected
        } else {
            R.drawable.ic_marker_city
        }
        return ContextCompat.getDrawable(requireContext(), icon)!!
    }

    private fun isLocationEnabled(): Boolean {
        val manager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate() {
        viewModel.onLocationUpdateRequested()
        val locationRequest = LocationRequest.Builder(LOCATION_REQUEST_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(1 * 24 * 60 * 60 * 1_000) // 1 day
            .setMaxUpdates(1)
            .build()
        fusedLocationProvider.requestLocationUpdates(locationRequest, locationUpdatesCallback, null)
    }

    private fun hasLocationPermission(): Boolean {
        @PermissionResult
        val permissionCheckResult = ContextCompat.checkSelfPermission(
            requireContext(),
            PERMISSION_LOCATION
        )
        return permissionCheckResult == PackageManager.PERMISSION_GRANTED
    }

    private fun IGeoPoint.toDoubleArray(): DoubleArray {
        return DoubleArray(2).apply {
            set(0, latitude)
            set(1, longitude)
        }
    }

    private fun DoubleArray.toGeoPoint(): GeoPoint {
        check(size == 2) { "Invalid array size: $size. Expected 2" }
        return GeoPoint(get(0), get(1))
    }

    private fun GeoPoint.toGeoCoordinate(): GeoCoordinate =
        GeoCoordinate(latitude = latitude, longitude = longitude)

    private fun GeoCoordinate.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

    companion object {
        private const val PERMISSION_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

        private const val LOCATION_REQUEST_INTERVAL: Long = 7000
        private const val OVERLAY_SEARCH_POINT = 1
        private const val ANIMATION_SPEED: Long = 300
        private const val MIN_ZOOM_LEVEL: Double = 2e0
        private const val MAP_LISTENER_DELAY: Long = 200
    }
}

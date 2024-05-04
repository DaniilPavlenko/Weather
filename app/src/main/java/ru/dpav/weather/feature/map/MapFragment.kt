package ru.dpav.weather.feature.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
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
import ru.dpav.weather.CitiesRepository
import ru.dpav.weather.R
import ru.dpav.weather.api.model.City
import ru.dpav.weather.databinding.FragmentMapBinding
import ru.dpav.weather.feature.city_details.CityDetailFragment
import ru.dpav.weather.ui.GoogleApiAvailabilityChecker

class MapFragment : MvpAppCompatFragment(), ru.dpav.weather.feature.map.MapView {

    private var binding: FragmentMapBinding? = null
    private var mapView: MapView? = null

    private lateinit var mFusedLocation: FusedLocationProviderClient
    private var isInfoWindowOpened: Boolean = false
    private var markers: ArrayList<Marker> = arrayListOf()

    @InjectPresenter
    lateinit var presenter: MapPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        binding?.run {
            locationButton.setOnClickListener { view ->
                if (view.isActivated) {
                    presenter.onLocationDisable()
                } else {
                    presenter.onLocationEnable()
                }
            }

            mapView = map
            initMap(map)

        }

        val appContext = requireContext().applicationContext
        Configuration.getInstance()
            .load(appContext, PreferenceManager.getDefaultSharedPreferences(appContext))

        val activity = requireActivity()
        if (GoogleApiAvailabilityChecker.isAvailable(activity)) {
            mFusedLocation = LocationServices.getFusedLocationProviderClient(activity)
        } else {
            presenter.onServicesUnavailable()
        }

        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        mapView = null
    }

    override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        Configuration.getInstance().load(requireContext(), prefs)
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        Configuration.getInstance().save(requireContext(), prefs)
        mapView?.onPause()
    }

    private fun initMap(map: MapView) {
        with(map) {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            val maxMapLatitude = MapView.getTileSystem().maxLatitude
            setScrollableAreaLimitLatitude(
                maxMapLatitude,
                -maxMapLatitude,
                (height / 2)
            )
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            minZoomLevel = MIN_ZOOM_LEVEL
            controller.setZoom(MapDefaults.DEFAULT_ZOOM)
            isVerticalMapRepetitionEnabled = false
        }
        setMapListener(map)
        addEventReceiver(map)
    }

    private fun setMapListener(map: MapView) {
        map.addMapListener(DelayedMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean = setCurrentPosition()

            override fun onZoom(event: ZoomEvent?): Boolean = setCurrentPosition()

            fun setCurrentPosition(): Boolean {
                val latitude = map.mapCenter.latitude
                val longitude = map.mapCenter.longitude
                val point = GeoPoint(latitude, longitude)
                presenter.onSetCurrentPosition(point, map.zoomLevelDouble)
                return true
            }
        }, MAP_LISTENER_DELAY))
    }

    private fun addEventReceiver(map: MapView) {
        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun longPressHelper(point: GeoPoint?): Boolean {
                presenter.onInfoWindowClose()
                return false
            }

            override fun singleTapConfirmedHelper(point: GeoPoint?): Boolean {
                point?.let {
                    if (isInfoWindowOpened) {
                        presenter.onInfoWindowClose()
                        return false
                    }
                    presenter.onMapClick(point)
                }
                return true
            }
        })
        map.overlays.add(eventsOverlay)
    }

    override fun setMapMarker(point: GeoPoint) {
        val binding = checkNotNull(binding)
        val icon = if (binding.locationButton.isActivated) {
            R.drawable.ic_marker_location
        } else {
            R.drawable.ic_marker_search_final
        }
        val searchIconOverlay = IconOverlay(
            point,
            ContextCompat.getDrawable(requireContext(), icon)
        )
        if (binding.map.overlays.lastIndex < OVERLAY_SEARCH_POINT) {
            binding.map.overlays.add(OVERLAY_SEARCH_POINT, searchIconOverlay)
            return
        }
        with(binding.map) {
            overlays[OVERLAY_SEARCH_POINT] = searchIconOverlay
            presenter.onCameraMoveTo(point, zoomLevelDouble)
            invalidate()
        }
    }

    override fun updateCitiesMarkers() {
        mapView?.let { map ->
            addMarkersOnMap(map, CitiesRepository.cities, markers, R.drawable.ic_marker_city)
            map.invalidate()
        }
    }

    private fun addMarkersOnMap(
        map: MapView,
        cities: List<City>,
        markersList: ArrayList<Marker>,
        markersIcon: Int,
    ) {
        map.overlays.removeAll(markersList)
        markersList.clear()
        cities.forEachIndexed { _, city ->
            markersList.add(
                makeMarker(city, markersIcon)
            )
        }
        map.overlays.addAll(markersList)
    }

    private fun makeMarker(
        city: City,
        icon: Int,
    ): Marker {
        val mapView = checkNotNull(mapView)

        val marker = Marker(mapView)
        with(marker) {
            id = city.id.toString()
            position = GeoPoint(
                city.coordinates.latitude,
                city.coordinates.longitude
            )
            setAnchor(Marker.ANCHOR_CENTER, 1f)
        }
        marker.icon = ContextCompat.getDrawable(requireContext(), icon)
        val infoWindow: PopInfoWindow

        infoWindow = PopInfoWindow(
            R.layout.info_window_weather,
            mapView,
            city,
            View.OnClickListener { openDetailDialog(city) }
        )

        marker.infoWindow = infoWindow
        marker.setOnMarkerClickListener { _, _ ->
            presenter.onMarkerClick(marker.id)
            return@setOnMarkerClickListener true
        }
        return marker
    }

    private fun openDetailDialog(city: City) {
        val detailFragment = CityDetailFragment.newInstance(city.id)
        detailFragment.show(requireActivity().supportFragmentManager, "dialog_city")
    }

    override fun openInfoWindow(cityId: String) {
        closeInfoWindow()
        isInfoWindowOpened = true
        mapView?.overlays?.forEach {
            if (it is Marker && it.id == cityId) {
                it.showInfoWindow()
                val icon = getMarkerIcon(isSelected = true)
                it.icon = icon
            }
        }
    }

    override fun closeInfoWindow() {
        isInfoWindowOpened = false
        val window = InfoWindow.getOpenedInfoWindowsOn(mapView)
        window.forEach {
            val relatedObject = it.relatedObject
            if (relatedObject is Marker) {
                val icon = getMarkerIcon(isSelected = false)
                relatedObject.icon = icon
            }
        }
        InfoWindow.closeAllInfoWindowsOn(mapView)
    }

    override fun moveCameraTo(point: GeoPoint, zoom: Double?) {
        mapView?.controller?.animateTo(point, zoom ?: mapView?.zoomLevelDouble, ANIMATION_SPEED)
    }

    override fun setCurrentPosition(point: GeoPoint, zoom: Double) {
        mapView?.controller?.animateTo(point, zoom, 0)
    }

    override fun setStartPosition() {
        if (hasLocationPermission()) {
            if (!isLocationEnabled()) {
                presenter.onLocationIsDisabled()
                presenter.onMoveToDefaultPosition()
                return
            }
        } else {
            presenter.onLocationIsDisabled()
            presenter.onMoveToDefaultPosition()
            return
        }
        getLocation(true)
    }

    override fun enableLocation(enable: Boolean) {
        if (enable) {
            if (isLocationEnabled() && hasLocationPermission()) {
                closeInfoWindow()
                binding?.locationButton?.isActivated = true
                getLocation(false)
            } else {
                askPermission()
                presenter.onLocationIsDisabled()
            }
        } else {
            binding?.locationButton?.isActivated = false
            mFusedLocation.removeLocationUpdates(mLocationCallBack)
        }
    }

    override fun showUpdateScreen(shown: Boolean) {
        val visibility = if (shown) View.VISIBLE else View.GONE
        binding?.mapUpdateLayout?.visibility = visibility
    }

    override fun showConnectionError(shown: Boolean) {
        if (shown) {
            view?.let {
                Snackbar.make(
                    it,
                    R.string.connection_error,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.retry) {
                    presenter.onRetryConnection()
                }.show()
            }
        }
    }

    override fun showSnack(resId: Int) {
        view?.let { itView ->
            Snackbar.make(
                itView,
                getString(R.string.geo_disabled),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasLocationPermission() || !hasStoragePermission()) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    REQUEST_PERMISSIONS
                )
            }
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

    private fun getLocation(isGetOnce: Boolean) {
        val locationRequest = LocationRequest.create()
        with(locationRequest) {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = LOCATION_REQUEST_INTERVAL
            fastestInterval = LOCATION_REQUEST_FAST_INTERVAL
            smallestDisplacement = LOCATION_REQUEST_DISPLACEMENT
            if (isGetOnce) numUpdates = 1
        }
        GoogleApiClient.Builder(requireActivity())
            .addApi(LocationServices.API)
            .addConnectionCallbacks(
                object : GoogleApiClient.ConnectionCallbacks {
                    override fun onConnectionSuspended(p0: Int) {}

                    @SuppressLint("MissingPermission")
                    override fun onConnected(p0: Bundle?) {
                        mFusedLocation.requestLocationUpdates(
                            locationRequest, mLocationCallBack, null
                        )
                    }
                })
            .build()
            .connect()
    }

    val mLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(location: LocationResult) {
            mapView?.let { mapView ->
                val lastLocation = location.lastLocation ?: return
                val point = GeoPoint(lastLocation.latitude, lastLocation.longitude)
                locateToPosition(point, mapView.zoomLevelDouble)
            } ?: error("MapView is null")
        }

        private fun locateToPosition(point: GeoPoint, zoom: Double) {
            activity?.let {
                with(presenter) {
                    onSetMarker(point)
                    onCameraMoveTo(point, zoom)
                }
            }
        }
    }

    private fun hasLocationPermission(): Boolean =
        hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun hasStoragePermission(): Boolean =
        hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun hasPermission(permissionName: String): Boolean {
        activity?.let {
            return ContextCompat.checkSelfPermission(
                it as Context,
                permissionName
            ) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    setStartPosition()
                }
            }
            else -> return
        }
    }

    companion object {
        private const val LOCATION_REQUEST_INTERVAL: Long = 7000
        private const val LOCATION_REQUEST_FAST_INTERVAL: Long = 4000
        private const val LOCATION_REQUEST_DISPLACEMENT: Float = 1F
        private const val REQUEST_PERMISSIONS: Int = 1
        private const val OVERLAY_SEARCH_POINT = 1
        private const val ANIMATION_SPEED: Long = 300
        private const val MIN_ZOOM_LEVEL: Double = 2e0
        private const val MAP_LISTENER_DELAY: Long = 200

        fun newInstance(): MapFragment = MapFragment()
    }
}

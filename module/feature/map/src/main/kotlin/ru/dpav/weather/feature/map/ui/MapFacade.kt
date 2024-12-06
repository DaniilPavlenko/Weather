package ru.dpav.weather.feature.map.ui

import android.Manifest
import android.app.Activity
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.IconOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import ru.dpav.weather.core.model.CityWeather
import ru.dpav.weather.core.model.GeoCoordinate
import ru.dpav.weather.feature.map.R
import ru.dpav.weather.feature.map.ui.extension.toGeoCoordinate

private typealias ZoomLevel = Double

internal class MapFacade(
    private val initialMarkerPosition: GeoPoint,
    private val startMapPosition: Pair<GeoPoint, ZoomLevel>,
    private val onRequestWeatherAt: (coordinate: GeoCoordinate) -> Unit,
    private val onLocationUpdateComplete: () -> Unit,
    private val onLocationUpdateCancel: () -> Unit,
    private val onOpenDetails: (cityWeather: CityWeather) -> Unit,
    private val mapStateSaver: (mapCenter: DoubleArray, zoom: Double) -> Unit,
) {

    private var mapView: MapView? = null
    private var ownerLifecycle: Lifecycle? = null

    private var isInfoWindowOpened: Boolean = false
    private var citiesFolderOverlay: FolderOverlay? = null
    private var displayedCities: List<CityWeather>? = null
    private var searchMarker: IconOverlay? = null

    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private val locationUpdatesCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(location: LocationResult) {
                onLocationUpdateComplete()

                val lastLocation = location.lastLocation ?: return
                val geoPoint = GeoPoint(lastLocation.latitude, lastLocation.longitude)

                onRequestWeatherAt(geoPoint.toGeoCoordinate())

                setSearchMarker(geoPoint, isUserLocation = true)
                moveCameraTo(geoPoint)
            }
        }
    }

    fun attachMap(mapView: MapView, activity: Activity, ownerLifecycle: Lifecycle) {
        Configuration.getInstance().userAgentValue = "weather-app"

        this.mapView = mapView
        this.ownerLifecycle = ownerLifecycle

        initMap(mapView)

        if (isLocationServicesAvailable(activity)) {
            fusedLocationProvider = LocationServices.getFusedLocationProviderClient(activity)
        }

        ownerLifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_RESUME) mapView.onResume()
                if (event == Lifecycle.Event.ON_PAUSE) mapView.onPause()
                if (event == Lifecycle.Event.ON_DESTROY) {
                    this@MapFacade.ownerLifecycle?.removeObserver(this)
                    this@MapFacade.ownerLifecycle = null
                    onDestroy()
                }
            }
        })
    }

    private fun initMap(mapView: MapView) = with(mapView) {
        val tileSource = TileSourceFactory.MAPNIK
        setTileSource(tileSource)
        setMultiTouchControls(true)

        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        maxZoomLevel = tileSource.maximumZoomLevel.toDouble()
        minZoomLevel = MIN_ZOOM_LEVEL

        val maxMapLatitude = MapView.getTileSystem().maxLatitude
        setScrollableAreaLimitLatitude(maxMapLatitude, -maxMapLatitude, 0)

        setPositionChangeListener(mapView)
        setTapsListener(mapView)

        citiesFolderOverlay = FolderOverlay()
        overlays.add(citiesFolderOverlay)

        val (startPosition, startZoom) = startMapPosition
        setMapPosition(startPosition, startZoom)
        setSearchMarker(initialMarkerPosition)
    }

    private fun onDestroy() {
        fusedLocationProvider?.let { locationProvider ->
            locationProvider.removeLocationUpdates(locationUpdatesCallback)
            onLocationUpdateCancel()
            fusedLocationProvider = null
        }

        searchMarker = null
        displayedCities = null
        citiesFolderOverlay = null
        mapView = null
    }

    private fun setPositionChangeListener(mapView: MapView) {
        val mapListener = object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean = setCurrentPosition()
            override fun onZoom(event: ZoomEvent): Boolean = setCurrentPosition()

            fun setCurrentPosition(): Boolean {
                val zoomLevel = mapView.zoomLevelDouble
                val mapCenter = mapView.mapCenter.toDoubleArray()
                mapStateSaver(mapCenter, zoomLevel)
                return true
            }
        }
        mapView.addMapListener(DelayedMapListener(mapListener, MAP_LISTENER_DELAY))
    }

    private fun setTapsListener(mapView: MapView) {
        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun longPressHelper(point: GeoPoint): Boolean {
                closeInfoWindows()
                return false
            }

            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                if (isInfoWindowOpened) {
                    closeInfoWindows()
                    return false
                }

                onRequestWeatherAt(point.toGeoCoordinate())
                setSearchMarker(point)
                moveCameraTo(point)
                return true
            }
        })
        mapView.overlays.add(eventsOverlay)
    }

    fun updateCities(citiesWeather: List<CityWeather>) {
        val map = requireMap()
        if (citiesWeather == displayedCities) {
            return
        }
        displayedCities = citiesWeather
        val citiesFolder = checkNotNull(citiesFolderOverlay)
        with(citiesFolder.items) {
            clear()
            for (city in citiesWeather) {
                add(createCityMarker(map, city))
            }
        }
        map.invalidate()
    }

    private fun createCityMarker(
        mapView: MapView,
        cityWeather: CityWeather,
    ) = Marker(mapView).apply {
        position = GeoPoint(cityWeather.coordinate.latitude, cityWeather.coordinate.longitude)
        icon = getCityMarkerIcon(isSelected = false)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        infoWindow = CityWeatherInfoWindow(mapView, cityWeather, onWindowClick = onOpenDetails)
        setOnMarkerClickListener { marker, _ ->
            openInfoWindow(marker)
            return@setOnMarkerClickListener true
        }
    }

    private fun openInfoWindow(marker: Marker) {
        closeInfoWindows()
        isInfoWindowOpened = true
        marker.showInfoWindow()
        marker.icon = getCityMarkerIcon(isSelected = true)
    }

    private fun closeInfoWindows() {
        isInfoWindowOpened = false
        val windows = InfoWindow.getOpenedInfoWindowsOn(requireMap())
        for (window in windows) {
            window.close()
            val relatedObject = window.relatedObject
            if (relatedObject is Marker) {
                relatedObject.icon = getCityMarkerIcon(isSelected = false)
            }
        }
    }

    private fun setSearchMarker(point: GeoPoint, isUserLocation: Boolean = false) {
        val map = requireMap()
        val icon = if (isUserLocation) {
            R.drawable.ic_marker_location
        } else {
            R.drawable.ic_marker_search_final
        }
        val searchMarker = searchMarker?.apply { set(point, getDrawable(icon)) }
            ?: IconOverlay(point, getDrawable(icon)).also { searchMarker = it }

        if (searchMarker !in map.overlays) {
            map.overlays += searchMarker
        }

        map.invalidate()
    }

    private fun setMapPosition(coordinate: GeoPoint, zoom: Double) {
        with(requireMap().controller) {
            setCenter(coordinate)
            setZoom(zoom)
        }
    }

    fun isLocationServicesAvailable(activity: Activity): Boolean {
        return ru.dpav.weather.feature.map.util.GoogleApiAvailabilityChecker.isAvailable(activity)
    }

    @RequiresPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    fun requestLocationUpdate() {
        val locationProvider = checkNotNull(fusedLocationProvider)

        closeInfoWindows()

        val locationRequest = LocationRequest.Builder(LOCATION_REQUEST_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(1 * 24 * 60 * 60 * 1_000) // 1 day
            .setMaxUpdates(1)
            .build()
        locationProvider.requestLocationUpdates(locationRequest, locationUpdatesCallback, null)
    }

    private fun getCityMarkerIcon(isSelected: Boolean): Drawable {
        val icon = if (isSelected) R.drawable.ic_marker_city_selected else R.drawable.ic_marker_city
        return getDrawable(icon)
    }

    private fun moveCameraTo(
        point: GeoPoint,
        zoom: Double? = null,
        animationSpeed: Long = ANIMATION_SPEED,
    ) {
        val map = requireMap()
        map.controller.animateTo(point, zoom ?: map.zoomLevelDouble, animationSpeed)
    }

    private fun IGeoPoint.toDoubleArray(): DoubleArray {
        return DoubleArray(2).apply {
            set(0, latitude)
            set(1, longitude)
        }
    }

    private fun getDrawable(@DrawableRes drawableRes: Int): Drawable {
        return requireNotNull(ContextCompat.getDrawable(requireMap().context, drawableRes))
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun requireMap() = requireNotNull(mapView)

    companion object {
        private const val MIN_ZOOM_LEVEL: Double = 2.0
        private const val MAP_LISTENER_DELAY: Long = 500
        private const val ANIMATION_SPEED: Long = 300
        private const val LOCATION_REQUEST_INTERVAL: Long = 7000
    }
}

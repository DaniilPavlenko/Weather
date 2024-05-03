package ru.dpav.weather.feature.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
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
import kotlinx.android.synthetic.main.fragment_map.view.locationButton
import kotlinx.android.synthetic.main.fragment_map.view.map
import kotlinx.android.synthetic.main.fragment_map.view.mapUpdateLayout
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
import ru.dpav.weather.AddCityActivity
import ru.dpav.weather.CitiesRepository
import ru.dpav.weather.R
import ru.dpav.weather.api.City
import ru.dpav.weather.feature.city_details.CityDetailFragment
import ru.dpav.weather.util.Util.Companion.isGooglePlayAvailable

class MapFragment : MvpAppCompatFragment(), ru.dpav.weather.feature.map.MapView {
    lateinit var mMap: MapView
    private lateinit var mLocationButton: ImageButton
    private lateinit var mFusedLocation: FusedLocationProviderClient
    private var isInfoWindowOpened: Boolean = false
    private lateinit var mUpdateScreen: FrameLayout
    private var mMarkers: ArrayList<Marker> = arrayListOf()
    private var mCustomMarkers: ArrayList<Marker> = arrayListOf()

    @InjectPresenter
    lateinit var mMapPresenter: MapPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater
            .inflate(R.layout.fragment_map, container, false)
        mUpdateScreen = view.mapUpdateLayout
        mLocationButton = view.locationButton
        mLocationButton.setOnClickListener {
            if (mLocationButton.isActivated) {
                mMapPresenter.onLocationDisable()
            } else {
                mMapPresenter.onLocationEnable()
            }
        }
        mMap = view.map
        initMap()
        activity?.let {
            val context = it.applicationContext
            Configuration.getInstance().load(
                context,
                PreferenceManager.getDefaultSharedPreferences(context)
            )
            if (isGooglePlayAvailable(it)) {
                mFusedLocation = LocationServices.getFusedLocationProviderClient(it)
            } else {
                mMapPresenter.onServicesUnavailable()
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        Configuration.getInstance().load(activity, prefs)
        mMap.onResume()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    REQUEST_NEW_CITY -> {
                        mMapPresenter.saveCity()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        Configuration.getInstance().save(activity, prefs)
        mMap.onPause()
    }

    private fun initMap() {
        with(mMap) {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            val maxMapLatitude = MapView.getTileSystem().maxLatitude
            setScrollableAreaLimitLatitude(
                maxMapLatitude,
                -maxMapLatitude,
                (height / 2)
            )
            zoomController.setVisibility(
                CustomZoomButtonsController.Visibility.NEVER
            )
            minZoomLevel = MIN_ZOOM_LEVEL
            controller.setZoom(MapDefaults.DEFAULT_ZOOM)
            isVerticalMapRepetitionEnabled = false
        }
        setMapListener()
        addEventReceiver()
    }

    private fun setMapListener() {
        mMap.addMapListener(DelayedMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean =
                setCurrentPosition()

            override fun onZoom(event: ZoomEvent?): Boolean =
                setCurrentPosition()

            fun setCurrentPosition(): Boolean {
                val latitude = mMap.mapCenter.latitude
                val longitude = mMap.mapCenter.longitude
                val point = GeoPoint(latitude, longitude)
                mMapPresenter.onSetCurrentPosition(point, mMap.zoomLevelDouble)
                return true
            }
        }, MAP_LISTENER_DELAY))
    }

    private fun addEventReceiver() {
        val mReceive: MapEventsReceiver = object : MapEventsReceiver {
            override fun longPressHelper(point: GeoPoint?): Boolean {
                point?.let {
                    mMapPresenter.onInfoWindowClose()
                    val city = City.getEmpty()
                    with(city.coordinates) {
                        latitude = point.latitude
                        longitude = point.longitude
                    }
                    openEditor(city)
                }
                return false
            }

            override fun singleTapConfirmedHelper(point: GeoPoint?): Boolean {
                point?.let {
                    if (isInfoWindowOpened) {
                        mMapPresenter.onInfoWindowClose()
                        return false
                    }
                    mMapPresenter.onMapClick(point)
                }
                return true
            }
        }
        val eventsOverlay = MapEventsOverlay(mReceive)
        mMap.overlays.add(eventsOverlay)
    }

    override fun setMapMarker(point: GeoPoint) {
        val icon = if (mLocationButton.isActivated) {
            R.drawable.ic_marker_location
        } else {
            R.drawable.ic_marker_search_final
        }
        val mSearchPoint = IconOverlay(
            point,
            ContextCompat.getDrawable(
                activity!!,
                icon
            )
        )
        if (mMap.overlays.lastIndex < OVERLAY_SEARCH_POINT) {
            mMap.overlays.add(OVERLAY_SEARCH_POINT, mSearchPoint)
            return
        }
        mMap.overlays[OVERLAY_SEARCH_POINT] = mSearchPoint
        mMapPresenter.onCameraMoveTo(point, mMap.zoomLevelDouble)
        mMap.invalidate()
    }

    override fun updateCitiesMarkers() {
        addMarkersOnMap(
            CitiesRepository.cities,
            mMarkers,
            R.drawable.ic_marker_city
        )
        addMarkersOnMap(
            CitiesRepository.customCities,
            mCustomMarkers,
            R.drawable.ic_marker_city_custom
        )
        mMap.invalidate()
    }

    private fun addMarkersOnMap(
        cities: List<City>,
        markersList: ArrayList<Marker>,
        markersIcon: Int,
    ) {
        mMap.overlays.removeAll(markersList)
        markersList.clear()
        cities.forEachIndexed { _, city ->
            markersList.add(
                makeMarker(city, markersIcon)
            )
        }
        mMap.overlays.addAll(markersList)
    }

    private fun openEditor(city: City) {
        activity?.let { act ->
            val addCityActivity = if (city.id == 0) {
                AddCityActivity.newIntent(act, city)
            } else {
                mMapPresenter.setEditingMarkerId(city.id.toString())
                AddCityActivity.newIntent(act, city.id)
            }
            startActivityForResult(addCityActivity, REQUEST_NEW_CITY)
        }
    }

    private fun makeMarker(
        city: City,
        icon: Int,
    ): Marker {
        val marker = Marker(mMap)
        with(marker) {
            id = city.id.toString()
            position = GeoPoint(
                city.coordinates.latitude,
                city.coordinates.longitude
            )
            setAnchor(Marker.ANCHOR_CENTER, 1f)
        }
        marker.icon = ContextCompat.getDrawable(activity!!, icon)
        val infoWindow: PopInfoWindow
        if (icon == R.drawable.ic_marker_city) {
            infoWindow = PopInfoWindow(
                R.layout.info_window_weather,
                mMap,
                city,
                View.OnClickListener { openDetailDialog(city) })
        } else {
            infoWindow = EditablePopInfo(
                R.layout.info_window_weather,
                mMap,
                city,
                object : EditablePopInfo.WindowInfoListener {
                    override fun onWindowClick() {
                        openDetailDialog(city)
                    }

                    override fun onEditClick() {
                        openEditor(city)
                    }

                    override fun onRemoveClick() {
                        mMapPresenter.onRemoveClick(city)
                    }
                }
            )
            marker.isDraggable = true
            marker.setOnMarkerDragListener(mDragListener)
        }

        marker.infoWindow = infoWindow
        marker.setOnMarkerClickListener { _, _ ->
            mMapPresenter.onMarkerClick(marker.id)
            return@setOnMarkerClickListener true
        }
        return marker
    }

    private val mDragListener = object : Marker.OnMarkerDragListener {
        override fun onMarkerDrag(marker: Marker?) {}

        override fun onMarkerDragStart(marker: Marker?) {
            mMapPresenter.onInfoWindowClose()
        }

        override fun onMarkerDragEnd(marker: Marker?) {
            marker?.let {
                mMapPresenter.onCustomCityDragEnd(it)
                marker.icon = getMarkerIcon(
                    marker.id.toInt(),
                    isSelected = false
                )
            }
        }
    }

    override fun addCustomCity(city: City) {
        val marker = makeMarker(
            city,
            R.drawable.ic_marker_city_custom
        )
        var editingIndex: Int? = null
        mCustomMarkers.forEachIndexed { index, it ->
            if (it.id == marker.id) {
                editingIndex = index
                return@forEachIndexed
            }
        }
        editingIndex?.let {
            mCustomMarkers.removeAt(it)
        }
        mCustomMarkers.add(marker)
        with(mMap) {
            overlays.forEachIndexed { index, overlay ->
                if (overlay is Marker && overlay.id.toInt() == city.id) {
                    mMap.overlays.removeAt(index)
                    return@forEachIndexed
                }
            }
            overlays.add(marker)
            invalidate()
        }
    }

    private fun openDetailDialog(city: City) {
        val detailFragment = CityDetailFragment.newInstance(city.id)
        detailFragment.show(
            activity?.supportFragmentManager,
            "dialog_city"
        )
    }

    override fun openInfoWindow(cityId: String) {
        closeInfoWindow()
        isInfoWindowOpened = true
        mMap.overlays.forEach {
            if (it is Marker && it.id == cityId) {
                it.showInfoWindow()
                val icon = getMarkerIcon(
                    it.id.toInt(),
                    isSelected = true
                )
                it.icon = icon
            }
        }
    }

    override fun closeInfoWindow() {
        isInfoWindowOpened = false
        val window = InfoWindow.getOpenedInfoWindowsOn(mMap)
        window.forEach {
            val relatedObject = it.relatedObject
            if (relatedObject is Marker) {
                val icon = getMarkerIcon(
                    relatedObject.id.toInt(),
                    isSelected = false
                )
                relatedObject.icon = icon
            }
        }
        InfoWindow.closeAllInfoWindowsOn(mMap)
    }

    override fun moveCameraTo(
        point: GeoPoint,
        zoom: Double?,
    ) {
        mMap.controller.animateTo(
            point,
            zoom ?: mMap.zoomLevelDouble,
            ANIMATION_SPEED
        )
    }

    override fun setCurrentPosition(
        point: GeoPoint,
        zoom: Double,
    ) {
        mMap.controller.animateTo(point, zoom, 0)
    }

    override fun setStartPosition() {
        if (hasLocationPermission()) {
            if (!isLocationEnabled()) {
                mMapPresenter.onLocationIsDisabled()
                mMapPresenter.onMoveToDefaultPosition()
                return
            }
        } else {
            mMapPresenter.onLocationIsDisabled()
            mMapPresenter.onMoveToDefaultPosition()
            return
        }
        getLocation(true)
    }

    override fun enableLocation(enable: Boolean) {
        if (enable) {
            if (isLocationEnabled() && hasLocationPermission()) {
                closeInfoWindow()
                mLocationButton.isActivated = true
                getLocation(false)
            } else {
                askPermission()
                mMapPresenter.onLocationIsDisabled()
            }
        } else {
            mLocationButton.isActivated = false
            mFusedLocation.removeLocationUpdates(mLocationCallBack)
        }
    }

    override fun showUpdateScreen(shown: Boolean) {
        val visibility = if (shown) View.VISIBLE else View.GONE
        mUpdateScreen.visibility = visibility
    }

    override fun showConnectionError(shown: Boolean) {
        if (shown) {
            view?.let {
                Snackbar.make(
                    it,
                    R.string.connection_error,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.retry) {
                    mMapPresenter.onRetryConnection()
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

    override fun showRemoveDialog(shown: Boolean) {
        if (!shown) return
        AlertDialog.Builder(activity)
            .setTitle(getString(R.string.remove))
            .setMessage(getString(R.string.remove_dialog_question))
            .setPositiveButton(getString(R.string.remove)) { _, _ ->
                mMapPresenter.onAcceptDialog()
                mMapPresenter.onCustomCityRemoved()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                mMapPresenter.onDeclineDialog()
            }
            .setOnCancelListener {
                mMapPresenter.onDeclineDialog()
            }
            .show()
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

    private fun getMarkerIcon(
        cityId: Int,
        isSelected: Boolean,
    ): Drawable {
        val icon: Int =
            if (CitiesRepository.isCustom(cityId)) {
                if (isSelected) {
                    R.drawable.ic_marker_city_custom_selected
                } else {
                    R.drawable.ic_marker_city_custom
                }
            } else {
                if (isSelected) {
                    R.drawable.ic_marker_city_selected
                } else {
                    R.drawable.ic_marker_city
                }
            }
        return ContextCompat.getDrawable(activity!!, icon)!!
    }

    private fun isLocationEnabled(): Boolean {
        val manager =
            activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
        GoogleApiClient.Builder(activity!!)
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
        override fun onLocationResult(location: LocationResult?) {
            location?.lastLocation?.let {
                val point = GeoPoint(
                    it.latitude,
                    it.longitude
                )
                locateToPosition(point, mMap.zoomLevelDouble)
            }
        }

        private fun locateToPosition(
            point: GeoPoint,
            zoom: Double,
        ) {
            activity?.let {
                with(mMapPresenter) {
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
        private const val REQUEST_NEW_CITY = 1
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

package ru.dpav.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.dpav.weather.api.City
import ru.dpav.weather.api.WeatherApi
import ru.dpav.weather.api.WeatherResponse
import ru.dpav.weather.util.Util
import ru.dpav.weather.util.Util.Companion.bitmapDescriptorFromVector
import ru.dpav.weather.util.Util.Companion.isGooglePlayServicesAvailable
import java.io.IOException

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mSearchMarker: Marker? = null
    private lateinit var mLastLatLng: LatLng
    private var mMarkers: LinkedHashMap<Int, Marker> = LinkedHashMap()
    private lateinit var mUpdateScreen: FrameLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mUpdateScreen = view.findViewById(R.id.map_update_layout)
        if (isGooglePlayServicesAvailable(activity!!)) {
            setUpdateScreen(true)
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
        } else {
            setUpdateScreen(false)
        }
        val mapFragment: SupportMapFragment? = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        if (savedState != null) {
            arguments = savedState
        } else {
            askPermission()
        }
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::mMap.isInitialized && ::mLastLatLng.isInitialized) {
            outState.putDouble(ARG_CAMERA_LATITUDE, mMap.cameraPosition.target.latitude)
            outState.putDouble(ARG_CAMERA_LONGITUDE, mMap.cameraPosition.target.longitude)
            outState.putFloat(ARG_CAMERA_ZOOM, mMap.cameraPosition.zoom)
            outState.putDouble(ARG_POINT_LATITUDE, mLastLatLng.latitude)
            outState.putDouble(ARG_POINT_LONGITUDE, mLastLatLng.longitude)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap ?: return
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.setOnMapClickListener {
            setSearchMarker(it)
            moveCameraTo(it, mMap.cameraPosition.zoom)
        }
        mMap.setOnMarkerClickListener {
            if (it.tag == MARKER_SEARCH_TAG) {
                return@setOnMarkerClickListener true
            }
            it.showInfoWindow()
            true
        }
        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker?): View? = null
            @SuppressLint("InflateParams")
            override fun getInfoContents(marker: Marker?): View? {
                marker ?: return null
                val view = layoutInflater.inflate(R.layout.info_window_weather, null)

                val title: TextView = view.findViewById(R.id.info_window_title)
                title.text = marker.title
                val icon = Util.getWeatherIconByName(marker.snippet.split("icon=")[1])
                title.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
                val snippet: TextView = view.findViewById(R.id.info_window_snippet)
                snippet.text = marker.snippet.split("icon=")[0]
                return view
            }
        })
        if (arguments != null) {
            moveCameraTo(
                LatLng(arguments!!.getDouble(ARG_CAMERA_LATITUDE),
                    arguments!!.getDouble(ARG_CAMERA_LONGITUDE)),
                arguments!!.getFloat(ARG_CAMERA_ZOOM)
            )
            setSearchMarker(LatLng(arguments!!.getDouble(ARG_POINT_LATITUDE),
                arguments!!.getDouble(ARG_POINT_LONGITUDE)))
        } else {
            moveToStartPosition()
        }
    }

    private fun setSearchMarker(latLng: LatLng) {
        mSearchMarker?.remove()
        mSearchMarker = mMap.addMarker(MarkerOptions()
            .position(latLng)
            .icon(bitmapDescriptorFromVector(activity!!, R.drawable.marker_search)))
        mSearchMarker?.tag = MARKER_SEARCH_TAG
        mLastLatLng = latLng
        setUpdateScreen(true)
        getWeather(latLng)
    }

    private fun getWeather(latLng: LatLng) {
        activity ?: return
        WeatherApi.getInstance().getWeatherByCoordinates(activity as Context, latLng,
            object : Callback<WeatherResponse> {
                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    if (t is IOException) {
                        view?.let {
                            Snackbar.make(it, R.string.connection_error, Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.retry) { getWeather(latLng) }
                                .show()
                        }
                    }
                }

                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    activity ?: return
                    val cities = response.body()?.cities
                    if (cities == null) {
                        Toast.makeText(activity, response.body()?.message, Toast.LENGTH_SHORT).show()
                        return
                    }
                    updateMapMarkers(cities)
                    setUpdateScreen(false)
                    val listFragment = activity?.supportFragmentManager?.fragments!![1] as ListFragment
                    listFragment.onWeatherGot(cities)
                }
            })
    }

    private fun updateMapMarkers(cities: List<City>?) {
        mMarkers.values.forEach { (it).remove() }
        mMarkers.clear()
        cities?.forEach {
            val latLng = LatLng(it.coordinates!!.latitude, it.coordinates.longitude)
            mMarkers[it.id] = mMap.addMarker(MarkerOptions()
                .position(latLng)
                .icon(bitmapDescriptorFromVector(activity as Context, R.drawable.marker_city))
                .title(it.name)
                .snippet(getString(R.string.marker_snippet,
                    it.main!!.temp.toInt(),
                    it.wind!!.speed.toInt(),
                    it.clouds!!.all,
                    it.main.pressure.toInt(),
                    it.weather!![0].icon)
                )
            )
        }
    }

    private fun moveToStartPosition() {
        if (hasLocationPermission()) {
            if (!isLocationEnabled()) {
                view?.let { itView ->
                    Snackbar.make(itView, R.string.geo_disabled, Snackbar.LENGTH_LONG).show()
                }
                moveToDefaultPosition()
            }
            getLocation()
        } else {
            moveToDefaultPosition()
        }
    }

    private fun isLocationEnabled() = try {
        val lm = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    } catch (e: Exception) {
        false
    }

    private fun getLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.numUpdates = 1
        val googleApiClient: GoogleApiClient = GoogleApiClient.Builder(activity!!).addApi(LocationServices.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnectionSuspended(p0: Int) {}
                @SuppressLint("MissingPermission")
                override fun onConnected(p0: Bundle?) {
                    mFusedLocationProviderClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                        override fun onLocationResult(location: LocationResult?) {
                            location?.let {
                                val latLng = LatLng(it.lastLocation.latitude, it.lastLocation.longitude)
                                setSearchMarker(latLng)
                                moveCameraTo(latLng, DEFAULT_ZOOM)
                            }
                        }
                    }, null)
                }
            })
            .build()
        googleApiClient.connect()
    }

    private fun askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasLocationPermission()) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_PERMISSIONS_LOCATION)
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        activity?.let {
            return ContextCompat.checkSelfPermission(it as Context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    private fun moveToDefaultPosition() {
        val defaultLatLng = LatLng(
            getString(R.string.defaultLatitude).toDouble(),
            getString(R.string.defaultLongitude).toDouble())
        setSearchMarker(defaultLatLng)
        moveCameraTo(defaultLatLng, DEFAULT_ZOOM)
    }

    private fun moveCameraTo(latLng: LatLng, zoom: Float) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    override fun onRequestPermissionsResult(requestCode: Int,
        permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS_LOCATION -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    moveToStartPosition()
                }
            }
            else -> return
        }
    }

    fun setUpdateScreen(isVisible: Boolean) {
        mUpdateScreen.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    interface OnWeatherGotCallback {
        fun onWeatherGot(cities: List<City>?)
    }

    companion object {
        private const val REQUEST_PERMISSIONS_LOCATION: Int = 111
        private const val DEFAULT_ZOOM: Float = 10.5F
        private const val MARKER_SEARCH_TAG = 5
        private const val ARG_CAMERA_LATITUDE = "camera_latitude"
        private const val ARG_CAMERA_LONGITUDE = "camera_longitude"
        private const val ARG_CAMERA_ZOOM = "camera_zoom"
        private const val ARG_POINT_LATITUDE = "point_latitude"
        private const val ARG_POINT_LONGITUDE = "point_longitude"

        @JvmStatic
        fun newInstance(): MapFragment = MapFragment()
    }
}
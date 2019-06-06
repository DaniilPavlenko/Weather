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
import android.widget.ImageButton
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

class MapFragment : Fragment(), OnMapReadyCallback, WeatherApi.ResponseListener {
	private lateinit var mMap: GoogleMap
	private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
	private var mSearchMarker: Marker? = null
	private lateinit var mLastLatLng: LatLng
	private var mMarkers: LinkedHashMap<Int, Marker> = LinkedHashMap()
	private var mCities: List<City> = ArrayList()
	private lateinit var mUpdateScreen: FrameLayout
	private var isInfoWindowOpened: Boolean = false
	private var isListenGeolocation: Boolean = false
	private lateinit var openedWindowMarkerPosition: LatLng
	private lateinit var mLocationListenButton: ImageButton

	override fun onCreateView(inflater: LayoutInflater,
		container: ViewGroup?, savedState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_map, container, false)
		mUpdateScreen = view.findViewById(R.id.map_update_layout)
		mLocationListenButton = view.findViewById(R.id.location_listen_button)
		mLocationListenButton.setOnClickListener {
			if (mLocationListenButton.isActivated) {
				disableGeoListen()
			} else {
				enableGeoListen()
			}
		}
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
		if (::mMap.isInitialized && ::mLastLatLng.isInitialized && mLastLatLng.longitude != 0e0) {
			outState.putDouble(ARG_CAMERA_LATITUDE, mMap.cameraPosition.target.latitude)
			outState.putDouble(ARG_CAMERA_LONGITUDE, mMap.cameraPosition.target.longitude)
			outState.putFloat(ARG_CAMERA_ZOOM, mMap.cameraPosition.zoom)
			outState.putDouble(ARG_POINT_LATITUDE, mLastLatLng.latitude)
			outState.putDouble(ARG_POINT_LONGITUDE, mLastLatLng.longitude)
			outState.putParcelableArrayList(ARG_CITIES_DATA, mCities as ArrayList)
			if (isListenGeolocation) {
				outState.putBoolean(ARG_IS_LISTEN_GEOLOCATION, true)
			}
			if (isInfoWindowOpened) {
				outState.putParcelable(ARG_OPENED_INFO_WINDOW, openedWindowMarkerPosition)
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		WeatherApi.getInstance().detachListener()
	}

	override fun onMapReady(googleMap: GoogleMap?) {
		mMap = googleMap ?: return
		mMap.uiSettings.isMapToolbarEnabled = false
		mMap.uiSettings.isMyLocationButtonEnabled = true
		mMap.setOnMapClickListener {
			if (!isInfoWindowOpened) {
				disableGeoListen()
				setSearchMarker(it)
				moveCameraTo(it, mMap.cameraPosition.zoom)
			} else {
				isInfoWindowOpened = false
			}
		}
		mMap.setOnMarkerClickListener {
			if (it.tag == MARKER_SEARCH_TAG) {
				return@setOnMarkerClickListener true
			}
			it.showInfoWindow()
			isInfoWindowOpened = true
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
				openedWindowMarkerPosition = marker.position
				return view
			}
		})
		mMap.setOnInfoWindowClickListener {
			var position = 0
			for (i in 0 until mCities.size) {
				if (mCities[i].name == it.title) {
					position = i
				}
			}
			val cityFragment = CityDetailFragment.newInstance(mCities[position])
			cityFragment.show(activity?.supportFragmentManager, "dialog_city")
		}
		if (arguments != null) {
			isListenGeolocation = arguments!!.getBoolean(ARG_IS_LISTEN_GEOLOCATION, false)
			if (isListenGeolocation) {
				enableGeoListen()
			} else {
				moveCameraTo(LatLng(
					arguments!!.getDouble(ARG_CAMERA_LATITUDE),
					arguments!!.getDouble(ARG_CAMERA_LONGITUDE)
				), arguments!!.getFloat(ARG_CAMERA_ZOOM))
				setSearchMarker(LatLng(
					arguments!!.getDouble(ARG_POINT_LATITUDE),
					arguments!!.getDouble(ARG_POINT_LONGITUDE)
				))
			}
		} else {
			moveToStartPosition()
		}
		WeatherApi.getInstance().attachListener(this)
	}

	private fun setSearchMarker(latLng: LatLng) {
		mSearchMarker?.remove()
		mSearchMarker = mMap.addMarker(
			MarkerOptions()
				.position(latLng)
				.icon(bitmapDescriptorFromVector(activity!!, R.drawable.marker_search))
		)
		mSearchMarker?.tag = MARKER_SEARCH_TAG
		mLastLatLng = latLng
		if (arguments != null && arguments!!.containsKey(ARG_CITIES_DATA)) {
			val cities: List<City> = arguments!!.getParcelableArrayList(ARG_CITIES_DATA)!!
			updateMapMarkers(cities)
			setCitiesForList(cities)
			val openedWindow: LatLng? = arguments!!.getParcelable(ARG_OPENED_INFO_WINDOW)
			openedWindow?.let {
				openedWindowMarkerPosition = openedWindow
				mMarkers.forEach {
					if (it.value.position == openedWindowMarkerPosition) {
						it.value.showInfoWindow()
						openedWindowMarkerPosition = it.value.position
					}
				}
				isInfoWindowOpened = true
			}
			arguments?.remove(ARG_CITIES_DATA)
			setUpdateScreen(false)
		} else {
			getWeather(latLng)
		}
	}

	private fun setCitiesForList(cities: List<City>) {
		val listFragment = activity?.supportFragmentManager?.fragments!![1] as ListFragment
		listFragment.onWeatherGot(cities)
	}

	private fun getWeather(latLng: LatLng) {
		activity ?: return
		if (!isListenGeolocation) setUpdateScreen(true)
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
					if (activity != null) {
						processApiResponse(response)
					} else {
						WeatherApi.getInstance().setUnfinishedResponse(response)
					}
				}
			})
	}

	fun processApiResponse(response: Response<WeatherResponse>) {
		val cities = response.body()?.cities
		if (cities == null) {
			Toast.makeText(activity, response.body()?.message, Toast.LENGTH_SHORT).show()
			return
		}
		setUpdateScreen(false)
		if (isListenGeolocation && mCities == cities) {
			return
		}
		updateMapMarkers(cities)
		setCitiesForList(cities)
	}

	override fun onUnfinishedResponse(response: Response<WeatherResponse>) {
		processApiResponse(response)
	}

	private fun updateMapMarkers(cities: List<City>?) {
		mMarkers.values.forEach { (it).remove() }
		mMarkers.clear()
		if (cities == null) {
			return
		}
		mCities = cities
		mCities.forEach {
			val latLng = LatLng(it.coordinates!!.latitude, it.coordinates.longitude)
			mMarkers[it.id] = mMap.addMarker(
				MarkerOptions()
					.position(latLng)
					.icon(bitmapDescriptorFromVector(activity as Context, R.drawable.marker_city))
					.title(it.name)
					.snippet(
						getString(
							R.string.marker_snippet,
							it.main!!.temp.toInt(),
							it.wind!!.speed.toInt(),
							it.clouds!!.all,
							it.main.pressure.toInt(),
							it.weather!![0].icon
						)
					)
			)
		}
	}

	private fun moveToStartPosition() {
		if (hasLocationPermission()) {
			if (!isLocationEnabled()) {
				showGeoDisabled()
				moveToDefaultPosition()
			}
			getLocation()
		} else {
			moveToDefaultPosition()
		}
	}

	private fun isLocationEnabled(): Boolean {
		val manager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
			|| manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
	}

	private fun getLocation() {
		val locationRequest = LocationRequest.create()
		locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
		locationRequest.interval = 5000
		locationRequest.smallestDisplacement = 1f
		if (!isListenGeolocation) {
			locationRequest.numUpdates = 1
		}
		val googleApiClient = GoogleApiClient.Builder(activity!!).addApi(LocationServices.API)
			.addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
				override fun onConnectionSuspended(p0: Int) {}
				@SuppressLint("MissingPermission")
				override fun onConnected(p0: Bundle?) {
					mFusedLocationProviderClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
						override fun onLocationResult(location: LocationResult?) {
							location?.let {
								val latLng = LatLng(it.lastLocation.latitude, it.lastLocation.longitude)
								if (!::mLastLatLng.isInitialized) {
									mLastLatLng = latLng
									val zoom = if (mMap.cameraPosition.zoom > 3) {
										mMap.cameraPosition.zoom
									} else {
										DEFAULT_ZOOM
									}
									locateToPosition(latLng, zoom)
								} else if (isListenGeolocation) {
									locateToPosition(latLng, mMap.cameraPosition.zoom)
								} else if (!isListenGeolocation) {
									mFusedLocationProviderClient.removeLocationUpdates(this)
								}
							}
						}
					}, null)
				}

				private fun locateToPosition(latLng: LatLng, zoom: Float) {
					activity?.let {
						setSearchMarker(latLng)
						moveCameraTo(latLng, zoom)
					}
				}
			})
			.build()
		googleApiClient.connect()
	}

	private fun askPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (!hasLocationPermission()) {
				requestPermissions(arrayOf(
					Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION
				), REQUEST_PERMISSIONS_LOCATION)
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
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED
				) {
					moveToStartPosition()
				}
			}
			else -> return
		}
	}

	private fun setUpdateScreen(isVisible: Boolean) {
		mUpdateScreen.visibility = if (isVisible) View.VISIBLE else View.GONE
	}

	private fun enableGeoListen() {
		if (isLocationEnabled()) {
			mLocationListenButton.isActivated = true
			isListenGeolocation = true
			mLocationListenButton.isActivated = true
			getLocation()
		} else {
			showGeoDisabled()
		}
	}

	private fun showGeoDisabled() {
		view?.let { itView ->
			Snackbar.make(itView, R.string.geo_disabled, Snackbar.LENGTH_LONG).show()
		}
	}

	private fun disableGeoListen() {
		mLocationListenButton.isActivated = false
		isListenGeolocation = false
		mLocationListenButton.isActivated = false
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
		private const val ARG_CITIES_DATA = "cities_data"
		private const val ARG_OPENED_INFO_WINDOW = "opened_window"
		private const val ARG_IS_LISTEN_GEOLOCATION = "listen_geolocation"

		@JvmStatic
		fun newInstance(): MapFragment = MapFragment()
	}
}
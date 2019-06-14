package ru.dpav.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ru.dpav.weather.api.City
import ru.dpav.weather.presenters.MapPresenter
import ru.dpav.weather.util.Util
import ru.dpav.weather.util.Util.Companion.bitmapDescriptorFromVector
import ru.dpav.weather.util.Util.Companion.isGooglePlayServicesAvailable
import ru.dpav.weather.views.MapView

class MapFragment : MvpMapFragment(), MapView {
	override val mapContainerId: Int = R.id.map
	private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
	private var mSearchMarker: Marker? = null
	private lateinit var mLastLatLng: LatLng
	private var mMarkers: LinkedHashMap<Int, Marker> = LinkedHashMap()
	private lateinit var mUpdateScreen: FrameLayout
	private var isInfoWindowOpened: Boolean = false
	private lateinit var mLocationListenButton: ImageButton
	private lateinit var detailFragment: CityDetailFragment

	@InjectPresenter(type = PresenterType.GLOBAL)
	lateinit var mMapPresenter: MapPresenter

	@ProvidePresenter(type = PresenterType.GLOBAL)
	fun provideMapPresenter(): MapPresenter {
		val presenter = MapPresenter()
		presenter.setContext(activity!!.applicationContext)
		return presenter
	}

	override fun onCreateView(inflater: LayoutInflater,
		container: ViewGroup?, savedState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_map, container, false)
		mUpdateScreen = view.findViewById(R.id.map_update_layout)
		mLocationListenButton = view.findViewById(R.id.location_listen_button)
		mLocationListenButton.setOnClickListener {
			if (mLocationListenButton.isActivated) {
				mMapPresenter.onLocationDisable()
			} else {
				mMapPresenter.onLocationEnable()
			}
		}
		if (isGooglePlayServicesAvailable(activity!!)) {
			mMapPresenter.onServicesAvailable()
			mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
		} else {
			mMapPresenter.onServicesUnavailable()
		}
		mMapPresenter.onAskPermission()
		return view
	}

	override fun onMapReady() {
		super.onMapReady()
		map.uiSettings.isMapToolbarEnabled = false
		map.uiSettings.isMyLocationButtonEnabled = false
		map.setOnMapClickListener {
			if (!isInfoWindowOpened) {
				mMapPresenter.onMapClick(it)
				mMapPresenter.onCameraMoveTo(it, map.cameraPosition.zoom)
			} else {
				mMapPresenter.onInfoWindowClose()
			}
		}
		map.setOnMarkerClickListener {
			if (it.tag == MARKER_SEARCH_TAG) {
				return@setOnMarkerClickListener true
			}
			mMapPresenter.onMarkerClick(it)
			true
		}
		map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
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
		map.setOnInfoWindowClickListener {
			mMapPresenter.onInfoWindowClick(it)
		}
		mMapPresenter.onMapReady()
	}

	override fun openInfoWindow(marker: Marker) {
		isInfoWindowOpened = true
		mMarkers.forEach {
			if (it.value.title == marker.title) {
				it.value.showInfoWindow()
			}
		}
	}

	override fun closeInfoWindow() {
		isInfoWindowOpened = false
	}

	override fun showDetailInfo(city: City) {
		detailFragment = CityDetailFragment.newInstance(city)
		detailFragment.show(activity?.supportFragmentManager, "dialog_city")
	}

	override fun setMapMarker(latLng: LatLng) {
		mSearchMarker?.remove()
		mSearchMarker = map.addMarker(
			MarkerOptions()
				.position(latLng)
				.icon(bitmapDescriptorFromVector(activity!!, R.drawable.marker_search))
		)
		mSearchMarker?.tag = MARKER_SEARCH_TAG
	}

	override fun showConnectionError(latLng: LatLng) {
		view?.let {
			Snackbar.make(it, R.string.connection_error, Snackbar.LENGTH_INDEFINITE)
				.setAction(R.string.retry) { mMapPresenter.onMapClick(latLng) }
				.show()
		}
	}

	override fun showToastError(message: String) {
		Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
	}

	override fun updateMapMarkers(cities: List<City>) {
		mMarkers.values.forEach { (it).remove() }
		mMarkers.clear()
		cities.forEach {
			val latLng = LatLng(it.coordinates!!.latitude, it.coordinates.longitude)
			mMarkers[it.id] = map.addMarker(
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

	override fun moveToStartPosition() {
		if (hasLocationPermission()) {
			if (!isLocationEnabled()) {
				mMapPresenter.onLocationIsDisabled()
				mMapPresenter.onMoveToDefaultPosition()
			}
			getLocation(true)
		} else {
			mMapPresenter.onMoveToDefaultPosition()
		}
	}

	private fun isLocationEnabled(): Boolean {
		val manager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
			|| manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
	}

	private fun getLocation(isGetOnce: Boolean) {
		val locationRequest = LocationRequest.create()
		locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
		locationRequest.interval = 7000
		locationRequest.fastestInterval = 4000
		locationRequest.smallestDisplacement = 1f
		if (isGetOnce) {
			locationRequest.numUpdates = 1
		}
		val googleApiClient = GoogleApiClient.Builder(activity!!).addApi(LocationServices.API)
			.addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
				override fun onConnectionSuspended(p0: Int) {}
				@SuppressLint("MissingPermission")
				override fun onConnected(p0: Bundle?) {
					mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallBack, null)
				}
			})
			.build()
		googleApiClient.connect()
	}

	var mLocationCallBack = object : LocationCallback() {
		override fun onLocationResult(location: LocationResult?) {
			location?.let {
				val latLng = LatLng(it.lastLocation.latitude, it.lastLocation.longitude)
				if (!::mLastLatLng.isInitialized) {
					mLastLatLng = latLng
					val zoom = if (map.cameraPosition.zoom > 3) {
						map.cameraPosition.zoom
					} else {
						DEFAULT_ZOOM
					}
					locateToPosition(latLng, zoom)
					return
				}
				locateToPosition(latLng, map.cameraPosition.zoom)
			}
		}

		private fun locateToPosition(latLng: LatLng, zoom: Float) {
			activity?.let {
				mMapPresenter.onSetMarker(latLng)
				mMapPresenter.onCameraMoveTo(latLng, zoom)
			}
		}
	}

	override fun askPermission() {
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

	override fun moveToDefaultPosition(latLng: LatLng) {
		mMapPresenter.onMapClick(latLng)
		mMapPresenter.onCameraMoveTo(latLng, DEFAULT_ZOOM)
	}

	override fun moveCameraTo(latLng: LatLng, zoom: Float?) {
		if (zoom == null) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, map.cameraPosition.zoom))
		} else {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
		}
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

	override fun showUpdateScreen() {
		mUpdateScreen.visibility = View.VISIBLE
	}

	override fun hideUpdateScreen() {
		mUpdateScreen.visibility = View.GONE
	}

	override fun enableLocation() {
		if (isLocationEnabled() && hasLocationPermission()) {
			mLocationListenButton.isActivated = true
			getLocation(false)
		} else {
			askPermission()
			mMapPresenter.onLocationIsDisabled()
		}
	}

	override fun showLocationIsDisabled() {
		view?.let { itView ->
			Snackbar.make(itView, R.string.geo_disabled, Snackbar.LENGTH_LONG).show()
		}
	}

	override fun disableLocation() {
		mLocationListenButton.isActivated = false
		mFusedLocationProviderClient.removeLocationUpdates(mLocationCallBack)
	}

	companion object {
		private const val REQUEST_PERMISSIONS_LOCATION: Int = 111
		private const val DEFAULT_ZOOM: Float = 10.5F
		private const val MARKER_SEARCH_TAG = 5

		@JvmStatic
		fun newInstance(): MapFragment = MapFragment()
	}
}
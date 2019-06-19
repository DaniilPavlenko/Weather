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
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.info_window_weather.view.*
import ru.dpav.weather.api.City
import ru.dpav.weather.presenters.MapPresenter
import ru.dpav.weather.util.Util
import ru.dpav.weather.util.Util.Companion.bitmapDescriptorFromVector
import ru.dpav.weather.util.Util.Companion.isGooglePlayAvailable
import ru.dpav.weather.views.MapView

class MapFragment : MvpMapFragment(), MapView {
	override val mapContainerId: Int = R.id.map
	private lateinit var mFusedLocation: FusedLocationProviderClient
	private var mSearchMarker: Marker? = null
	private var isStartPointShowed: Boolean = false
	private var mMarkers: LinkedHashMap<Int, Marker> = LinkedHashMap()
	private lateinit var mUpdateScreen: FrameLayout
	private var openedInfoMarker: Marker? = null
	private lateinit var mLocationListenButton: ImageButton
	private lateinit var detailFragment: CityDetailFragment

	@InjectPresenter
	lateinit var mMapPresenter: MapPresenter

	override fun onCreateView(inflater: LayoutInflater,
		container: ViewGroup?, savedState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_map, container, false)
		mUpdateScreen = view.map_update_layout
		mLocationListenButton = view.location_listen_button
		mLocationListenButton.setOnClickListener {
			if (mLocationListenButton.isActivated) {
				mMapPresenter.onLocationDisable()
			} else {
				mMapPresenter.onLocationEnable()
			}
		}
		if (isGooglePlayAvailable(activity!!)) {
			mMapPresenter.onServicesAvailable()
			mFusedLocation = LocationServices.getFusedLocationProviderClient(activity!!)
		} else {
			mMapPresenter.onServicesUnavailable()
		}
		return view
	}

	override fun onMapReady() {
		super.onMapReady()
		with(map) {
			with(uiSettings) {
				isMapToolbarEnabled = false
				isMyLocationButtonEnabled = false
			}
			setOnMapClickListener {
				if (openedInfoMarker == null) {
					mMapPresenter.onMapClick(it)
					mMapPresenter.onCameraMoveTo(it, cameraPosition.zoom)
				} else {
					mMapPresenter.onInfoWindowClose()
				}
			}
			setOnMarkerClickListener {
				if (it.tag == MARKER_SEARCH_TAG) {
					return@setOnMarkerClickListener true
				}
				openedInfoMarker = it
				mMapPresenter.onMarkerClick(it)
				true
			}
			setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
				override fun getInfoWindow(marker: Marker?): View? = null
				@SuppressLint("InflateParams")
				override fun getInfoContents(marker: Marker?): View? {
					marker ?: return null
					val view = layoutInflater.inflate(R.layout.info_window_weather, null)
					val title: TextView = view.info_window_title
					title.text = marker.title
					val icon = Util.getWeatherIconByName(marker.snippet.split("icon=")[1])
					title.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
					val snippet: TextView = view.info_window_snippet
					snippet.text = marker.snippet.split("icon=")[0]
					return view
				}
			})
			setOnInfoWindowClickListener {
				mMapPresenter.onInfoWindowClick(it)
			}
		}
	}

	override fun openInfoWindow(marker: Marker) {
		mMarkers.forEach {
			if (it.value.title == marker.title) {
				it.value.showInfoWindow()
				openedInfoMarker = it.value
				return@forEach
			}
		}
	}

	override fun closeInfoWindow() {
		openedInfoMarker?.hideInfoWindow()
		openedInfoMarker = null
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
			val latLng = LatLng(it.coordinates.latitude, it.coordinates.longitude)
			mMarkers[it.id] = map.addMarker(
				MarkerOptions()
					.position(latLng)
					.icon(bitmapDescriptorFromVector(activity as Context, R.drawable.marker_city))
					.title(it.name)
					.snippet(
						getString(
							R.string.marker_snippet,
							it.main.temp.toInt(),
							it.wind.speed.toInt(),
							it.clouds.cloudy,
							Util.getPressureInMmHg(it.main.pressure),
							it.weather[0].icon
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
		locationRequest.interval = LOCATION_REQUEST_INTERVAL
		locationRequest.fastestInterval = LOCATION_REQUEST_FAST_INTERVAL
		locationRequest.smallestDisplacement = LOCATION_REQUEST_DISPLACEMENT
		if (isGetOnce) {
			locationRequest.numUpdates = 1
		}
		val googleApiClient = GoogleApiClient.Builder(activity!!).addApi(LocationServices.API)
			.addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
				override fun onConnectionSuspended(p0: Int) {}
				@SuppressLint("MissingPermission")
				override fun onConnected(p0: Bundle?) {
					mFusedLocation.requestLocationUpdates(locationRequest, mLocationCallBack, null)
				}
			})
			.build()
		googleApiClient.connect()
	}

	var mLocationCallBack = object : LocationCallback() {
		override fun onLocationResult(location: LocationResult?) {
			location?.let {
				val latLng = LatLng(it.lastLocation.latitude, it.lastLocation.longitude)
				if (!isStartPointShowed) {
					isStartPointShowed = true
					val zoom = if (map.cameraPosition.zoom > MAP_INIT_ZOOM) {
						map.cameraPosition.zoom
					} else {
						Constants.DEFAULT_ZOOM
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
				requestPermissions(
					arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
					REQUEST_PERMISSIONS_LOCATION
				)
			}
		}
	}

	private fun hasLocationPermission(): Boolean {
		activity?.let {
			return ContextCompat.checkSelfPermission(
				it as Context,
				Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
		}
		return false
	}

	override fun moveCameraTo(latLng: LatLng, zoom: Float?) {
		val cameraZoom = zoom ?: map.cameraPosition.zoom
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, cameraZoom))
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

	override fun showUpdateScreen(isShow: Boolean) {
		mUpdateScreen.visibility = if (isShow) View.VISIBLE else View.GONE
	}

	override fun enableLocation(isEnable: Boolean) {
		if (isEnable) {
			if (isLocationEnabled() && hasLocationPermission()) {
				mLocationListenButton.isActivated = true
				getLocation(false)
			} else {
				askPermission()
				mMapPresenter.onLocationIsDisabled()
			}
		} else {
			mLocationListenButton.isActivated = false
			mFusedLocation.removeLocationUpdates(mLocationCallBack)
		}
	}

	override fun showLocationIsDisabled() {
		view?.let { itView ->
			Snackbar.make(itView, R.string.geo_disabled, Snackbar.LENGTH_LONG).show()
		}
	}

	companion object {
		private const val LOCATION_REQUEST_INTERVAL: Long = 7000
		private const val LOCATION_REQUEST_FAST_INTERVAL: Long = 4000
		private const val LOCATION_REQUEST_DISPLACEMENT: Float = 1F
		private const val REQUEST_PERMISSIONS_LOCATION: Int = 111
		private const val MARKER_SEARCH_TAG = 5
		private const val MAP_INIT_ZOOM = 3

		@JvmStatic
		fun newInstance(): MapFragment = MapFragment()
	}
}
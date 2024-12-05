package ru.dpav.weather.feature.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import ru.dpav.weather.R
import ru.dpav.weather.core.model.CityWeather
import ru.dpav.weather.core.model.GeoCoordinate
import ru.dpav.weather.databinding.FragmentMapBinding
import ru.dpav.weather.feature.cities_list.ListFragment
import ru.dpav.weather.feature.city_details.CityDetailsFragment
import ru.dpav.weather.ui.extension.toGeoCoordinate

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map) {

    private val viewModel: MapViewModel by viewModels()
    private var binding: FragmentMapBinding? = null
    private var mapFacade: MapFacade? = null

    private val permissionsRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result[PERMISSION_LOCATION] == true) {
            onLocationPermissionGranted()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(FragmentMapBinding.bind(view)) {
            binding = this

            btnShowListOfCities.setOnClickListener { navigateToList() }
            btnRequestLocation.setOnClickListener { onClickRequestLocation() }
        }

        initMapFacade()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    checkNotNull(mapFacade).updateCities(uiState.citiesWeather)
                    binding?.run {
                        mapUpdateLayout.isVisible = uiState.isLoading || uiState.isLocationUpdating
                        btnShowListOfCities.isVisible = uiState.citiesWeather.isNotEmpty()
                        btnRequestLocation.isEnabled = uiState.isLocationServicesAvailable
                    }
                    if (uiState.hasConnectionError) {
                        showConnectionError()
                    }
                    if (uiState.showTurnedOffGeoInfo) {
                        showSnack(R.string.geo_disabled, viewModel::onShownDisabledGeoInfo)
                    }
                }
        }

        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                mapFacade = null
                binding = null
            }
        })
    }

    private fun initMapFacade() {
        val mapCenter = viewModel.mapCenter.value.toGeoPoint()
        val zoom = viewModel.mapZoomLevel.value
        val markerPosition = viewModel.uiState.value.requestedPosition?.toGeoPoint() ?: mapCenter

        val mapFacade = MapFacade(
            initialMarkerPosition = markerPosition,
            startMapPosition = mapCenter to zoom,
            onRequestWeatherAt = viewModel::onRequestWeatherAt,
            onLocationUpdateComplete = viewModel::onLocationUpdateCompleted,
            onLocationUpdateCancel = viewModel::onLocationUpdateCancel,
            onOpenDetails = ::navigateToDetails,
            mapStateSaver = viewModel::saveMapState
        )

        val mapView = checkNotNull(binding).map
        mapFacade.attachMap(mapView, requireActivity(), viewLifecycleOwner.lifecycle)
        this.mapFacade = mapFacade

        viewModel.onRequestWeatherAt(markerPosition.toGeoCoordinate())

        if (mapFacade.isLocationServicesAvailable(requireActivity())) {
            viewModel.onLocationServiceAvailable()
        } else {
            viewModel.onLocationServiceUnavailable()
        }
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
        requestLocationUpdate()
    }

    private fun showConnectionError() {
        Snackbar.make(requireView(), R.string.connection_error, Snackbar.LENGTH_INDEFINITE).apply {
            setAction(R.string.retry) {
                viewModel.onRetryRequest()
                dismiss()
            }
            show()
        }
    }

    private fun navigateToDetails(cityWeather: CityWeather) {
        val detailFragment = CityDetailsFragment.newInstance(cityWeather.cityId)
        val screenTag = "details"
        parentFragmentManager.commit {
            replace(R.id.mainFragmentContainer, detailFragment, screenTag)
            addToBackStack(screenTag)
        }
    }

    private fun navigateToList() {
        parentFragmentManager.commit {
            val screenTag = "list"
            replace(R.id.mainFragmentContainer, ListFragment.newInstance(), screenTag)
            addToBackStack(screenTag)
        }
    }

    private fun showSnack(resId: Int, shownCallback: () -> Unit) {
        Snackbar.make(requireView(), getString(resId), Snackbar.LENGTH_LONG)
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
            permissionsRequestLauncher.launch(arrayOf(PERMISSION_LOCATION))
        }
    }

    private fun isLocationEnabled(): Boolean {
        val manager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate() {
        viewModel.onLocationUpdateRequested()
        checkNotNull(mapFacade).requestLocationUpdate()
    }

    private fun hasLocationPermission(): Boolean {
        @PermissionResult
        val permissionCheckResult = ContextCompat.checkSelfPermission(
            requireContext(),
            PERMISSION_LOCATION
        )
        return permissionCheckResult == PackageManager.PERMISSION_GRANTED
    }

    private fun DoubleArray.toGeoPoint(): GeoPoint {
        check(size == 2) { "Invalid array size: $size. Expected 2" }
        return GeoPoint(get(0), get(1))
    }

    private fun GeoCoordinate.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

    companion object {
        private const val PERMISSION_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    }
}

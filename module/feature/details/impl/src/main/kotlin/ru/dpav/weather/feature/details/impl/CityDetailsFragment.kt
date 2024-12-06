package ru.dpav.weather.feature.details.impl

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ru.dpav.weather.common.ui.WeatherIconAssociator
import ru.dpav.weather.common.ui.extension.popBackStackToRoot
import ru.dpav.weather.core.data.WeatherRepository
import ru.dpav.weather.feature.details.impl.databinding.FragmentCityDetailsBinding
import javax.inject.Inject
import ru.dpav.weather.common.strings.R as StringsR

@AndroidEntryPoint
class CityDetailsFragment : Fragment(R.layout.fragment_city_details) {

    @Inject
    lateinit var weatherRepository: WeatherRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentCityDetailsBinding.bind(view)

        val cityId = requireNotNull(arguments?.getInt(ARG_CITY_ID))
        val cityWeather = weatherRepository.citiesWeather.firstOrNull { it.cityId == cityId }
        if (cityWeather == null) {
            // Just back to the root fragment (map).
            parentFragmentManager.popBackStackToRoot()
            return
        }

        with(binding) {
            with(toolbar) {
                title = cityWeather.cityName
                setNavigationOnClickListener { parentFragmentManager.popBackStack() }
            }
            with(cityDetailTemperature) {
                text = getString(StringsR.string.detail_temperature, cityWeather.temperature)
                val icon = WeatherIconAssociator.getIconByWeatherType(
                    weatherType = cityWeather.weatherType,
                    isNight = cityWeather.isNight
                )
                setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
            }
            cityDetailWind.text =
                getString(StringsR.string.detail_wind, cityWeather.windSpeed.metersPerSecond)
            cityDetailCloudy.text =
                getString(StringsR.string.detail_cloudy, cityWeather.cloudiness.value)
            cityDetailPressure.text =
                getString(
                    StringsR.string.detail_pressure,
                    cityWeather.pressure.millimetersOfMercury
                )
            cityDetailHumidity.text = getString(
                StringsR.string.detail_humidity,
                cityWeather.humidity.value
            )
        }
    }

    companion object {
        private const val ARG_CITY_ID = "city_id"

        fun newInstance(cityId: Int) = CityDetailsFragment().apply {
            arguments = Bundle(1).apply {
                putInt(ARG_CITY_ID, cityId)
            }
        }
    }
}

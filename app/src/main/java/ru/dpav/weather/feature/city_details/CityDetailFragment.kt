package ru.dpav.weather.feature.city_details

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import ru.dpav.weather.CitiesRepository
import ru.dpav.weather.R
import ru.dpav.weather.databinding.FragmentCityDetailBinding
import ru.dpav.weather.ui.WeatherIconAssociator

class CityDetailFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle("City")
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        dialog?.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        val binding = FragmentCityDetailBinding.inflate(inflater, container, false)

        val cityId = requireNotNull(arguments?.let { it.getInt(ARG_CITY_ID) })
        val city = CitiesRepository.cities.first { it.id == cityId }

        with(binding) {
            cityDetailTitle.text = city.name
            with(cityDetailTemperature) {
                text = getString(R.string.detail_temperature, city.main.temp.toInt())
                val icon = WeatherIconAssociator.getIconByName(city.weather[0].icon)
                cityDetailTemperature.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
            }
            cityDetailWind.text = getString(R.string.detail_wind, city.wind.speed.toInt())
            cityDetailCloudy.text = getString(R.string.detail_cloudy, city.clouds.cloudy)
            cityDetailPressure.text = getString(R.string.detail_pressure, city.main.pressureInMmHg)
            cityDetailHumidity.text = getString(
                R.string.detail_humidity,
                city.main.humidity.toInt()
            )

            closeButton.setOnClickListener { dialog?.cancel() }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.run {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }
    }

    companion object {
        private const val ARG_CITY_ID = "city_id"

        fun newInstance(cityId: Int) = CityDetailFragment().apply {
            arguments = Bundle(1).apply {
                putInt(ARG_CITY_ID, cityId)
            }
        }
    }
}

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
import kotlinx.android.synthetic.main.fragment_city_detail.view.cityDetailCloudy
import kotlinx.android.synthetic.main.fragment_city_detail.view.cityDetailHumidity
import kotlinx.android.synthetic.main.fragment_city_detail.view.cityDetailPressure
import kotlinx.android.synthetic.main.fragment_city_detail.view.cityDetailTemperature
import kotlinx.android.synthetic.main.fragment_city_detail.view.cityDetailTitle
import kotlinx.android.synthetic.main.fragment_city_detail.view.cityDetailWind
import kotlinx.android.synthetic.main.fragment_city_detail.view.closeButton
import ru.dpav.weather.CitiesRepository
import ru.dpav.weather.R
import ru.dpav.weather.util.Util

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
    ): View? {
        dialog?.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        val view = inflater.inflate(
            R.layout.fragment_city_detail,
            container,
            false
        )
        arguments?.let { args ->

            val cityId = args.getInt(ARG_CITY_ID)

            val city = CitiesRepository.cities.first { it.id == cityId }

            view.cityDetailTitle.text = city.name

            view.cityDetailTemperature.text = getString(
                R.string.detail_temperature,
                city.main.temp.toInt()
            )

            val icon = Util.Icons.getWeatherIconByName(city.weather[0].icon)

            view.cityDetailTemperature
                .setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)

            view.cityDetailWind.text = getString(
                R.string.detail_wind,
                city.wind.speed.toInt()
            )

            view.cityDetailCloudy.text = getString(
                R.string.detail_cloudy,
                city.clouds.cloudy
            )

            view.cityDetailPressure.text = getString(
                R.string.detail_pressure,
                city.main.pressureInMmHg
            )

            view.cityDetailHumidity.text = getString(
                R.string.detail_humidity,
                city.main.humidity.toInt()
            )

            view.closeButton.setOnClickListener { dialog.cancel() }
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            it.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }
    }

    companion object {
        private const val ARG_CITY_ID = "city_id"
        fun newInstance(cityId: Int): CityDetailFragment =
            CityDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CITY_ID, cityId)
                }
            }
    }
}

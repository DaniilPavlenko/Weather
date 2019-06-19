package ru.dpav.weather

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import kotlinx.android.synthetic.main.fragment_city_detail.view.*
import ru.dpav.weather.api.City
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
		savedInstanceState: Bundle?): View? {
		dialog?.window?.apply {
			requestFeature(Window.FEATURE_NO_TITLE)
			setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		}
		val view = inflater.inflate(R.layout.fragment_city_detail, container, false)
		arguments?.let {
			view.city_detail_title.text = it.getString(ARG_CITY_NAME)

			view.city_detail_temperature.text = getString(R.string.detail_temperature, it.getInt(ARG_TEMPERATURE))

			val icon = Util.getWeatherIconByName(it.getString(ARG_WEATHER_ICON)!!)
			view.city_detail_temperature.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)

			view.city_detail_wind.text = getString(R.string.detail_wind, it.getInt(ARG_WIND))

			view.city_detail_cloudy.text = getString(R.string.detail_cloudy, it.getInt(ARG_CLOUDY))

			view.city_detail_pressure.text = getString(R.string.detail_pressure, it.getInt(ARG_PRESSURE))

			view.city_detail_humidity.text = getString(R.string.detail_humidity, it.getInt(ARG_HUMIDITY))

			view.close_button.setOnClickListener { dialog.cancel() }
		}
		return view
	}

	override fun onStart() {
		super.onStart()
		dialog?.window?.let {
			it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
			it.setBackgroundDrawable(ColorDrawable(Color.WHITE))
		}
	}

	companion object {
		private const val ARG_CITY_NAME = "city_name"
		private const val ARG_TEMPERATURE = "temperature"
		private const val ARG_WEATHER_ICON = "weather_icon"
		private const val ARG_WIND = "wind"
		private const val ARG_CLOUDY = "cloudy"
		private const val ARG_PRESSURE = "pressure"
		private const val ARG_HUMIDITY = "humidity"

		fun newInstance(city: City): CityDetailFragment = CityDetailFragment().apply {
			arguments = Bundle().apply {
				putString(ARG_CITY_NAME, city.name)
				putInt(ARG_TEMPERATURE, city.main.temp.toInt())
				putString(ARG_WEATHER_ICON, city.weather[0].icon)
				putInt(ARG_WIND, city.wind.speed.toInt())
				putInt(ARG_CLOUDY, city.clouds.cloudy)
				putInt(ARG_PRESSURE, Util.getPressureInMmHg(city.main.pressure))
				putInt(ARG_HUMIDITY, city.main.humidity.toInt())
			}
		}
	}
}
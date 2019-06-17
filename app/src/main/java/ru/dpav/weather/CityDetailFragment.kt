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
import android.widget.Button
import android.widget.TextView
import ru.dpav.weather.api.City
import ru.dpav.weather.util.Util

class CityDetailFragment : DialogFragment() {

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialog = super.onCreateDialog(savedInstanceState)
		dialog.setTitle("City")
		return dialog
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?): View? {
		dialog?.window?.apply {
			requestFeature(Window.FEATURE_NO_TITLE)
			setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		}
		val view = inflater.inflate(R.layout.fragment_city_detail, container, false)
		val city: City = arguments!!.getParcelable(ARG_CITY_KEY)!!

		val temperature = view.findViewById<TextView>(R.id.city_detail_temperature)
		temperature.text = getString(R.string.detail_temperature, city.main?.temp?.toInt())
		val icon = Util.getWeatherIconByName(city.weather!![0].icon)
		temperature.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)

		view.findViewById<TextView>(R.id.city_detail_title).text = city.name

		view.findViewById<TextView>(R.id.city_detail_wind).text =
			getString(R.string.detail_wind, city.wind?.speed?.toInt())

		view.findViewById<TextView>(R.id.city_detail_cloudy).text =
			getString(R.string.detail_cloudy, city.clouds?.all)

		view.findViewById<TextView>(R.id.city_detail_pressure).text =
			getString(R.string.detail_pressure, Util.getPressureInMmHg(city.main!!.pressure))

		view.findViewById<TextView>(R.id.city_detail_humidity).text =
			getString(R.string.detail_humidity, city.main.humidity.toInt())

		view.findViewById<Button>(R.id.close_button).setOnClickListener {
			dialog.cancel()
		}
		return view
	}

	override fun onStart() {
		super.onStart()
		val dialog = dialog
		if (dialog != null) {
			dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
			dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
		}
	}

	companion object {
		private const val ARG_CITY_KEY = "city_key"

		fun newInstance(city: City): CityDetailFragment = CityDetailFragment().apply {
			arguments = Bundle().apply {
				putParcelable(ARG_CITY_KEY, city)
			}
		}
	}
}
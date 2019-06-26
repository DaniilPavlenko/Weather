package ru.dpav.weather

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import kotlinx.android.synthetic.main.activity_add_city.*
import ru.dpav.weather.api.City
import ru.dpav.weather.presenters.AddCityPresenter
import ru.dpav.weather.util.Util
import ru.dpav.weather.views.AddCityView

class AddCityActivity : MvpAppCompatActivity(), AddCityView {

	private lateinit var mCity: City
	private val iconsArray = arrayOf(
		R.drawable.weather_icon_01,
		R.drawable.weather_icon_01n,
		R.drawable.weather_icon_02,
		R.drawable.weather_icon_02n,
		R.drawable.weather_icon_03,
		R.drawable.weather_icon_04,
		R.drawable.weather_icon_09,
		R.drawable.weather_icon_10,
		R.drawable.weather_icon_10n,
		R.drawable.weather_icon_11,
		R.drawable.weather_icon_13,
		R.drawable.weather_icon_50
	)

	@InjectPresenter
	lateinit var mAddCityPresenter: AddCityPresenter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_add_city)

		val imagesAdapter = ImagesArrayAdapter(this, iconsArray)
		weatherIconSpinner.adapter = imagesAdapter

		val percents: Array<Int> = (0..100).toList().toTypedArray()
		val percentAdapter = ArrayAdapter<Int>(
			this,
			android.R.layout.simple_spinner_dropdown_item,
			percents)

		cloudySpinner.adapter = percentAdapter
		humiditySpinner.adapter = percentAdapter

		cancelButton.setOnClickListener {
			finish()
		}

		saveButton.setOnClickListener {
			clearCityName()
			saveCityState()
			validateData()
		}
	}

	override fun onPause() {
		super.onPause()
		saveCityState()
	}

	override fun setCity(city: City) {
		mCity = city
	}

	override fun cancel() {
		finish()
	}

	override fun save() {
		setResult(Activity.RESULT_OK)
		finish()
	}

	private fun clearCityName() {
		var clearName = cityNameEdit.text.toString()
		clearName = clearName.trim()
		clearName = clearName.replace(Regex("\\s+"), " ")
		cityNameEdit.setText(clearName)
	}

	private fun saveCityState() {
		with(mCity) {
			with(coordinates) {
				if (latitude == 0e0 && longitude == 0e0) {
					intent.extras?.let {
						latitude = it.getDouble(ARG_LATITUDE)
						longitude = it.getDouble(ARG_LONGITUDE)
					}
				}
			}

			name = cityNameEdit.text.toString()

			main.temp = getFloatFromEdit(temperatureEdit)

			main.pressure = getFloatFromEdit(pressureEdit)

			main.humidity = getFloatFromSpinner(humiditySpinner)

			clouds.cloudy = getFloatFromSpinner(cloudySpinner).toInt()

			wind.speed = getFloatFromEdit(windEdit)

			val icon = weatherIconSpinner.selectedItem.toString().toInt()
			weather[0].icon = Util.getWeatherNameByIcon(icon)
		}
		mAddCityPresenter.onDataChanged(mCity)
	}

	private fun showSnack(message: String) {
		Snackbar.make(scrollView, message, Snackbar.LENGTH_LONG)
			.show()
	}

	private fun validateData() {
		if (mCity.name.replace(" ", "").length < 3) {
			showSnack(getString(R.string.error_valid_name))
			cityNameEdit.requestFocus()
			return
		} else if (!mCity.name.matches(Regex("[\\p{L} \\-]*"))) {
			showSnack(getString(R.string.error_valid_name_spec_chars))
			cityNameEdit.requestFocus()
			return
		}
		if (mCity.main.temp > 60 || mCity.main.temp < -90) {
			showSnack(getString(R.string.error_valid_temp))
			temperatureEdit.requestFocus()
			return
		}
		if (mCity.main.pressure < 700 || mCity.main.pressure > 800) {
			showSnack(getString(R.string.error_valid_pressure))
			pressureEdit.requestFocus()
			return
		}
		mAddCityPresenter.onSave(mCity)
	}

	private fun getFloatFromEdit(edit: EditText): Float {
		return try {
			edit.text.toString().toFloat()
		} catch (e: NumberFormatException) {
			0f
		}
	}

	private fun getFloatFromSpinner(spinner: Spinner): Float {
		return spinner.selectedItem.toString().toFloat()
	}

	companion object {
		private const val ARG_LATITUDE = "latitude"
		private const val ARG_LONGITUDE = "longitude"

		fun newIntent(context: Context, city: City): Intent {
			val intent = Intent(context, AddCityActivity::class.java)
			intent.putExtra(ARG_LATITUDE, city.coordinates.latitude)
			intent.putExtra(ARG_LONGITUDE, city.coordinates.longitude)
			return intent
		}
	}
}
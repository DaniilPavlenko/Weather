package ru.dpav.weather

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.activity_add_city.*
import ru.dpav.weather.api.City
import ru.dpav.weather.presenters.AddCityPresenter
import ru.dpav.weather.util.Util
import ru.dpav.weather.views.AddCityView

class AddCityActivity : MvpAppCompatActivity(), AddCityView {

	private val iconsArray = arrayOf(
		R.drawable.weather_icon_01,
		R.drawable.weather_icon_01n,
		R.drawable.weather_icon_50,
		R.drawable.weather_icon_02,
		R.drawable.weather_icon_02n,
		R.drawable.weather_icon_03,
		R.drawable.weather_icon_04,
		R.drawable.weather_icon_09,
		R.drawable.weather_icon_10,
		R.drawable.weather_icon_10n,
		R.drawable.weather_icon_11,
		R.drawable.weather_icon_13
	)

	@InjectPresenter
	lateinit var mAddCityPresenter: AddCityPresenter

	@ProvidePresenter
	fun providePresenter(): AddCityPresenter {
		val addCityPresenter = AddCityPresenter()
		intent.extras?.let { it ->
			val cityId = it.getInt(ARG_CITY_ID, 0)
			val city: City
			if (cityId != 0) {
				city = CitiesRepository.customCities
					.filter { it.id == cityId }[0]
			} else {
				city = City.getEmpty()
				with(city.coordinates) {
					latitude = it.getDouble(ARG_LATITUDE)
					longitude = it.getDouble(ARG_LONGITUDE)
				}
			}
			addCityPresenter.setCity(city)
		}
		return addCityPresenter
	}

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

		cancelButton.setOnClickListener { finish() }

		saveButton.setOnClickListener { validateData() }
	}

	override fun setCity(city: City) {
		with(city) {
			cityNameEdit.setText(name)

			temperatureEdit.setText(main.temp.toInt().toString())

			pressureEdit.setText(main.pressure.toString())

			humiditySpinner.setSelection(main.humidity.toInt() - 1)

			cloudySpinner.setSelection(clouds.cloudy - 1)

			windEdit.setText(wind.speed.toInt().toString())

			val icon = Util.getWeatherIconByName(weather[0].icon)
			iconsArray.forEachIndexed { index, value ->
				if (value == icon) {
					weatherIconSpinner.setSelection(index)
					return@forEachIndexed
				}
			}
		}

		deleteButton.visibility = View.VISIBLE
		deleteButton.setOnClickListener {
			mAddCityPresenter.onRemoveClick()
		}
	}

	override fun cancel() {
		finish()
	}

	override fun save() {
		setResult(Activity.RESULT_OK)
		finish()
	}

	override fun showRemoveDialog(shown: Boolean) {
		if (!shown) return
		AlertDialog.Builder(this)
			.setTitle(getString(R.string.remove))
			.setMessage(getString(R.string.remove_dialog_question))
			.setPositiveButton(getString(R.string.remove)) { _, _ ->
				mAddCityPresenter.onAcceptDialog()
				setResult(MapFragment.RESULT_REMOVE)
				finish()
			}
			.setNegativeButton(getString(R.string.cancel)) { _, _ ->
				mAddCityPresenter.onDeclineDialog()
			}
			.show()
	}

	private fun clearCityName() {
		val clearName = cityNameEdit.text.toString()
			.trim()
			.replace(Regex("\\s+"), " ")
		cityNameEdit.setText(clearName)
	}

	private fun showSnack(message: String) {
		Snackbar.make(scrollView, message, Snackbar.LENGTH_LONG)
			.show()
	}

	private fun validateData() {
		val cityName = cityNameEdit.text.toString()
			.replace(" ", "")
		if (cityName.length < MIN_CITY_NAME_LENGTH) {
			showSnack(getString(R.string.error_valid_name))
			cityNameEdit.requestFocus()
			return
		} else if (!cityName.matches(Regex("((?![a-zA-Z])[\\p{L} \\-])*"))) {
			showSnack(getString(R.string.error_valid_name_spec_chars))
			cityNameEdit.requestFocus()
			return
		}

		clearCityName()

		val temperature = getIntFromEdit(temperatureEdit)
		if (temperature !in MIN_TEMPERATURE..MAX_TEMPERATURE) {
			showSnack(getString(R.string.error_valid_temp))
			temperatureEdit.requestFocus()
			return
		}

		val weatherIconPosition = weatherIconSpinner.selectedItemPosition
		val cloudy = cloudySpinner.selectedItem as Int

		if (weatherIconPosition >= ICON_CLOUDY
			&& cloudy == 0) {
			showSnack(getString(R.string.error_valid_cloudy))
			return
		}

		val humidity = humiditySpinner.selectedItem as Int
		if (weatherIconPosition >= ICON_HUMIDITY && humidity == 0) {
			showSnack(getString(R.string.error_valid_humidity))
			return
		}

		if (weatherIconPosition == ICON_WIND
			&& getIntFromEdit(windEdit) == 0) {
			showSnack(getString(R.string.error_valid_wind))
			windEdit.requestFocus()
			return
		}

		val pressure = getIntFromEdit(pressureEdit)
		if (pressure !in MIN_PRESSURE..MAX_PRESSURE) {
			showSnack(getString(R.string.error_valid_pressure))
			pressureEdit.requestFocus()
			return
		}

		val city = City.getEmpty()
		with(city) {
			name = cityNameEdit.text.toString()
			main.temp = getFloatFromEdit(temperatureEdit)
			main.pressure = getFloatFromEdit(pressureEdit)
			main.humidity = getFloatFromSpinner(humiditySpinner)
			clouds.cloudy = getFloatFromSpinner(cloudySpinner).toInt()
			wind.speed = getFloatFromEdit(windEdit)
			val icon = weatherIconSpinner.selectedItem.toString().toInt()
			weather[0].icon = Util.getWeatherNameByIcon(icon)
		}

		mAddCityPresenter.onSave(city)
	}

	private fun getIntFromEdit(edit: EditText) = try {
		edit.text.toString().toInt()
	} catch (e: NumberFormatException) {
		0
	}

	private fun getFloatFromEdit(edit: EditText) = try {
		edit.text.toString().toFloat()
	} catch (e: NumberFormatException) {
		0f
	}

	private fun getFloatFromSpinner(spinner: Spinner): Float {
		return spinner.selectedItem.toString().toFloat()
	}

	companion object {
		private const val ARG_CITY_ID = "cityId"
		private const val ARG_LATITUDE = "latitude"
		private const val ARG_LONGITUDE = "longitude"

		private const val MAX_TEMPERATURE = 60
		private const val MIN_TEMPERATURE = -90
		private const val MIN_CITY_NAME_LENGTH = 3
		private const val MIN_PRESSURE = 665
		private const val MAX_PRESSURE = 815

		private const val ICON_WIND = 2
		private const val ICON_CLOUDY = 3
		private const val ICON_HUMIDITY = 7

		fun newIntent(context: Context, city: City): Intent {
			val intent = Intent(context, AddCityActivity::class.java)
			intent.putExtra(ARG_LATITUDE, city.coordinates.latitude)
			intent.putExtra(ARG_LONGITUDE, city.coordinates.longitude)
			return intent
		}

		fun newIntent(context: Context, cityId: Int): Intent {
			val intent = Intent(context, AddCityActivity::class.java)
			intent.putExtra(ARG_CITY_ID, cityId)
			return intent
		}
	}
}
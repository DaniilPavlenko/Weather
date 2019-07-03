package ru.dpav.weather

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_city_new.*
import ru.dpav.weather.api.City
import ru.dpav.weather.presenters.AddCityPresenter
import ru.dpav.weather.util.Util
import ru.dpav.weather.views.AddCityView

class AddCityActivity : MvpAppCompatActivity(), AddCityView {

	@InjectPresenter
	lateinit var mAddCityPresenter: AddCityPresenter

	@ProvidePresenter
	fun providePresenter(): AddCityPresenter {
		intent.extras!!.let {
			val city = City.getEmpty()
			val cityId = it.getInt(ARG_CITY_ID, 0)
			if (cityId != 0) {
				city.id = cityId
			} else {
				with(city.coordinates) {
					latitude = it.getDouble(ARG_LATITUDE)
					longitude = it.getDouble(ARG_LONGITUDE)
				}
			}
			return AddCityPresenter(city)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_add_city_new)
		editorChangeIcon.setOnClickListener { showChooseIconDialog() }
		editorCancelButton.setOnClickListener { finish() }
		editorSaveButton.setOnClickListener { validateData() }
		initNameEdit()
		initSeekBarTemperature()
		initSeekBarWind()
		initSeekBarCloudy()
		initSeekBarHumidity()
		initSeekBarPressure()
	}

	override fun onActivityResult(
		requestCode: Int,
		resultCode: Int,
		data: Intent?
	) {
		if (resultCode == Activity.RESULT_CANCELED) {
			return
		}
		if (resultCode == Activity.RESULT_OK
			&& requestCode == REQUEST_ICON
		) {
			data?.let {
				val iconRes = it.getIntExtra(
					ChooseIconActivity.SELECTED_ICON,
					R.drawable.weather_icon_01
				)
				mAddCityPresenter.onIconSelected(iconRes)
			}
		}
	}

	private fun showChooseIconDialog() {
		startActivityForResult(
			ChooseIconActivity.newIntent(this),
			REQUEST_ICON
		)
	}

	private fun setChangeButton() {
		editorSaveButton.text = getString(R.string.save)
	}

	override fun setCity(city: City) {
		setChangeButton()
		with(city) {
			editorNameEdit.setText(name)

			val temperature = main.temp.toInt() + TEMPERATURE_OFFSET
			temperatureSeekBar.progress = temperature

			val pressure = main.pressure.toInt() - PRESSURE_OFFSET
			pressureSeekBar.progress = pressure

			windSeekBar.progress = wind.speed.toInt()

			cloudySeekBar.progress = clouds.cloudy

			humiditySeekBar.progress = main.humidity.toInt()

			editorWeatherIcon.setImageDrawable(
				ContextCompat.getDrawable(
					this@AddCityActivity,
					Util.Icons.getWeatherIconByName(weather[0].icon)
				)
			)
		}
	}

	override fun cancel() {
		finish()
	}

	override fun save() {
		setResult(Activity.RESULT_OK)
		finish()
	}

	override fun setWeatherIcon(icon: Int) {
		editorWeatherIcon.setImageDrawable(
			AppCompatResources.getDrawable(this, icon)
		)
		isCloudyValid(icon)
		isHumidityValid(icon)
	}

	private fun clearCityName() {
		val clearName = editorNameEdit.text.toString()
			.trim()
			.replace(Regex("\\s+"), " ")
		editorNameEdit.setText(clearName)
	}

	private fun initNameEdit() {
		editorNameEdit.addTextChangedListener(
			object : TextWatcher {
				override fun afterTextChanged(s: Editable?) {}
				override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
				override fun onTextChanged(
					s: CharSequence?,
					start: Int,
					before: Int,
					count: Int
				) {
					isCityNameValid()
				}
			}
		)
	}

	private fun initSeekBarTemperature() {
		temperatureSeekBar.setOnSeekBarChangeListener(
			object : SeekBar.OnSeekBarChangeListener {
				override fun onStartTrackingTouch(seekBar: SeekBar?) {}
				override fun onStopTrackingTouch(seekBar: SeekBar?) {}
				override fun onProgressChanged(
					seekBar: SeekBar?,
					progress: Int,
					fromUser: Boolean
				) {
					editorTemperatureText.text = getString(
						R.string.temperature_placeholder,
						(progress - TEMPERATURE_OFFSET)
					)
				}
			}
		)
		val seekMaxValue = -MIN_TEMPERATURE + MAX_TEMPERATURE
		temperatureSeekBar.max = seekMaxValue
		temperatureSeekBar.progress = seekMaxValue - MAX_TEMPERATURE
	}

	private fun initSeekBarWind() {
		windSeekBar.max = MAX_WIND
		windSeekBar.setOnSeekBarChangeListener(
			object : SeekBar.OnSeekBarChangeListener {
				override fun onStartTrackingTouch(seekBar: SeekBar?) {}
				override fun onStopTrackingTouch(seekBar: SeekBar?) {}
				override fun onProgressChanged(
					seekBar: SeekBar?,
					progress: Int,
					fromUser: Boolean
				) {
					editorWindText.text = getString(
						R.string.wind_placeholder,
						progress
					)
				}
			}
		)
		windSeekBar.progress = 1
		windSeekBar.progress = 0
	}

	private fun initSeekBarCloudy() {
		cloudySeekBar.setOnSeekBarChangeListener(
			object : SeekBar.OnSeekBarChangeListener {
				override fun onStopTrackingTouch(seekBar: SeekBar?) {}
				override fun onStartTrackingTouch(seekBar: SeekBar?) {}
				override fun onProgressChanged(
					seekBar: SeekBar?,
					progress: Int,
					fromUser: Boolean
				) {
					editorCloudyText.text = getString(
						R.string.cloudy_placeholder,
						progress
					)
					isCloudyValid(mAddCityPresenter.iconResId)
				}
			}
		)
		editorCloudyText.text = getString(
			R.string.cloudy_placeholder,
			0
		)
	}

	private fun initSeekBarHumidity() {
		editorHumidityError.visibility = View.GONE
		humiditySeekBar.setOnSeekBarChangeListener(
			object : SeekBar.OnSeekBarChangeListener {
				override fun onStartTrackingTouch(seekBar: SeekBar?) {}
				override fun onStopTrackingTouch(seekBar: SeekBar?) {}
				override fun onProgressChanged(
					seekBar: SeekBar?,
					progress: Int,
					fromUser: Boolean
				) {
					editorHumidityText.text = getString(
						R.string.humidity_placeholder,
						progress
					)
					isHumidityValid(mAddCityPresenter.iconResId)
				}
			}
		)
		editorHumidityText.text = getString(
			R.string.humidity_placeholder,
			0
		)
	}

	private fun initSeekBarPressure() {
		pressureSeekBar.setOnSeekBarChangeListener(
			object : SeekBar.OnSeekBarChangeListener {
				override fun onStartTrackingTouch(seekBar: SeekBar?) {}
				override fun onStopTrackingTouch(seekBar: SeekBar?) {}
				override fun onProgressChanged(
					seekBar: SeekBar?,
					progress: Int,
					fromUser: Boolean
				) {
					val pressure = progress + PRESSURE_OFFSET
					editorPressureText.text = getString(
						R.string.pressure_placeholder,
						pressure
					)
				}
			}
		)
		val pressureMaxValue = MAX_PRESSURE - PRESSURE_OFFSET
		val defaultValue = pressureMaxValue - pressureMaxValue / 2
		pressureSeekBar.max = pressureMaxValue
		pressureSeekBar.progress = defaultValue
	}

	private fun validateData() {
		var error = false

		if (!isCityNameValid()) error = true

		val icon = mAddCityPresenter.iconResId

		if (!isCloudyValid(icon)) error = true

		if (!isHumidityValid(icon)) error = true

		if (error) {
			mAddCityPresenter.onSaveError()
			return
		}

		clearCityName()
		val city = createCityByData()
		mAddCityPresenter.onSave(city)
	}

	private fun isCityNameValid(): Boolean {
		val cityName = editorNameEdit.text.toString()
			.replace(" ", "")
		if (cityName.length < MIN_CITY_NAME_LENGTH) {
			mAddCityPresenter.onNameError(
				getString(R.string.error_valid_name)
			)
			return false
		} else if (!cityName.matches(Regex("((?![a-zA-Z])[\\p{L} \\-])*"))) {
			mAddCityPresenter.onNameError(
				getString(R.string.error_valid_name_spec_chars)
			)
			return false
		}
		mAddCityPresenter.onNameError(null)
		return true
	}

	private fun isCloudyValid(icon: Int): Boolean {
		val needCloudy = icon in Util.Icons.iconsCloudy
			|| icon in Util.Icons.iconsHumidity
		return if (needCloudy && cloudySeekBar.progress == 0) {
			mAddCityPresenter.onCloudyError(true)
			false
		} else {
			mAddCityPresenter.onCloudyError(false)
			true
		}
	}

	private fun isHumidityValid(icon: Int): Boolean {
		val needHumidity = icon in Util.Icons.iconsHumidity
		return if (needHumidity && humiditySeekBar.progress == 0) {
			mAddCityPresenter.onHumidityError(true)
			false
		} else {
			mAddCityPresenter.onHumidityError(false)
			true
		}
	}

	override fun showNameError(message: String?) {
		var visibility = View.GONE
		var drawable: Drawable? = null
		if (!message.isNullOrEmpty()) {
			visibility = View.VISIBLE
			drawable = ContextCompat
				.getDrawable(this, R.drawable.ic_error)
		}
		editorNameError.visibility = visibility
		editorNameError.text = message
		editorNameEdit.setCompoundDrawablesWithIntrinsicBounds(
			null,
			null,
			drawable,
			null
		)
	}

	override fun showSnackError() {
		Snackbar.make(
			headerConstraint,
			R.string.check_errors,
			Snackbar.LENGTH_SHORT
		).show()
	}

	override fun showHumidityError(shown: Boolean) {
		val visibility = if (shown) View.VISIBLE else View.GONE
		editorHumidityError.visibility = visibility
	}

	override fun showCloudyError(shown: Boolean) {
		val visibility = if (shown) View.VISIBLE else View.GONE
		editorCloudyError.visibility = visibility
	}

	private fun createCityByData(): City {
		val city = City.getEmpty()
		with(city) {
			name = editorNameEdit.text.toString()
			val temperature = temperatureSeekBar.progress - TEMPERATURE_OFFSET
			main.temp = temperature.toFloat()
			val pressure = pressureSeekBar.progress + PRESSURE_OFFSET
			main.pressure = pressure.toFloat()
			main.humidity = humiditySeekBar.progress.toFloat()
			clouds.cloudy = cloudySeekBar.progress
			wind.speed = windSeekBar.progress.toFloat()
			val icon = mAddCityPresenter.iconResId
			weather[0].icon = Util.Icons.getWeatherNameByIcon(icon)
		}
		return city
	}

	companion object {
		private const val REQUEST_ICON = 1

		private const val ARG_CITY_ID = "cityId"
		private const val ARG_LATITUDE = "latitude"
		private const val ARG_LONGITUDE = "longitude"

		private const val TEMPERATURE_OFFSET = 90
		private const val PRESSURE_OFFSET = 665

		private const val MAX_TEMPERATURE = 60
		private const val MIN_TEMPERATURE = -90
		private const val MIN_CITY_NAME_LENGTH = 3
		private const val MAX_PRESSURE = 815
		private const val MAX_WIND = 110

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
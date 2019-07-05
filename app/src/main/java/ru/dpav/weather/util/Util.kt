package ru.dpav.weather.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import ru.dpav.weather.R

class Util {
	class Icons {
		companion object {
			val iconsCloudy = arrayOf(
				R.drawable.weather_icon_02,
				R.drawable.weather_icon_02n,
				R.drawable.weather_icon_03,
				R.drawable.weather_icon_04
			)

			val iconsHumidity = arrayOf(
				R.drawable.weather_icon_09,
				R.drawable.weather_icon_10,
				R.drawable.weather_icon_10n,
				R.drawable.weather_icon_11,
				R.drawable.weather_icon_13,
				R.drawable.weather_icon_50
			)

			fun getWeatherIconByName(iconName: String) =
				when (iconName) {
					"01d" -> R.drawable.weather_icon_01
					"01n" -> R.drawable.weather_icon_01n
					"02d" -> R.drawable.weather_icon_02
					"02n" -> R.drawable.weather_icon_02n
					"03d", "03n" -> R.drawable.weather_icon_03
					"04d", "04n" -> R.drawable.weather_icon_04
					"09d", "09n" -> R.drawable.weather_icon_09
					"10d" -> R.drawable.weather_icon_10
					"10n" -> R.drawable.weather_icon_10n
					"11d", "11n" -> R.drawable.weather_icon_11
					"13d", "13n" -> R.drawable.weather_icon_13
					"50d" -> R.drawable.weather_icon_50
					else -> R.drawable.weather_icon_01
				}

			fun getWeatherNameByIcon(icon: Int): String =
				when (icon) {
					R.drawable.weather_icon_01 -> "01d"
					R.drawable.weather_icon_01n -> "01n"
					R.drawable.weather_icon_02 -> "02d"
					R.drawable.weather_icon_02n -> "02n"
					R.drawable.weather_icon_03 -> "03d"
					R.drawable.weather_icon_04 -> "04d"
					R.drawable.weather_icon_09 -> "09d"
					R.drawable.weather_icon_10 -> "10d"
					R.drawable.weather_icon_10n -> "10n"
					R.drawable.weather_icon_11 -> "11d"
					R.drawable.weather_icon_13 -> "13d"
					R.drawable.weather_icon_50 -> "50d"
					else -> "01d"
				}

			fun getDrawableIdByName(
				context: Context,
				name: String
			): Int {
				return context.resources.getIdentifier(
					name,
					"drawable",
					context.packageName
				)
			}
		}
	}

	companion object {
		private const val HPA_TO_MM_HG_COEFFICIENT = 1.333f
		private const val PLAY_SERVICES_CODE = 2404

		fun getPressureInMmHg(hpa: Float): Int =
			if (hpa > 850) {
				(hpa / HPA_TO_MM_HG_COEFFICIENT).toInt()
			} else {
				hpa.toInt()
			}

		fun isGooglePlayAvailable(activity: Activity): Boolean {
			val googleApi = GoogleApiAvailability.getInstance()
			val status: Int = googleApi.isGooglePlayServicesAvailable(activity)
			if (status != ConnectionResult.SUCCESS) {
				if (googleApi.isUserResolvableError(status)) {
					googleApi.getErrorDialog(
						activity,
						status,
						PLAY_SERVICES_CODE
					).show()
				}
				return false
			}
			return true
		}
	}
}
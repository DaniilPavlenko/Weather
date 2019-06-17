package ru.dpav.weather.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v4.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import ru.dpav.weather.R

class Util {
	companion object {
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
				else -> R.drawable.weather_icon_01
			}

		fun getPressureInMmHg(hpa: Float): Int = (hpa / 1.333f).toInt()

		fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
			val googleApiAvailability: GoogleApiAvailability = GoogleApiAvailability.getInstance()
			val status: Int = googleApiAvailability.isGooglePlayServicesAvailable(activity)
			if (status != ConnectionResult.SUCCESS) {
				if (googleApiAvailability.isUserResolvableError(status)) {
					googleApiAvailability.getErrorDialog(activity, status, 2404).show()
				}
				return false
			}
			return true
		}

		fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
			val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
				?: return BitmapDescriptorFactory.defaultMarker()
			vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
			val bitmap = Bitmap.createBitmap(
				vectorDrawable.intrinsicWidth,
				vectorDrawable.intrinsicHeight,
				Bitmap.Config.ARGB_8888
			)
			val canvas = Canvas(bitmap)
			vectorDrawable.draw(canvas)
			return BitmapDescriptorFactory.fromBitmap(bitmap)
		}
	}
}
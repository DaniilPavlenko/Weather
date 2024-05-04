package ru.dpav.weather.ui

import android.app.Activity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

internal object GoogleApiAvailabilityChecker {

    private const val PLAY_SERVICES_CODE = 2404

    fun isAvailable(activity: Activity): Boolean {
        GoogleApiAvailability.getInstance().run {
            val servicesStatus = isGooglePlayServicesAvailable(activity)
            if (servicesStatus == ConnectionResult.SUCCESS) {
                return true
            }

            if (isUserResolvableError(servicesStatus)) {
                getErrorDialog(activity, servicesStatus, PLAY_SERVICES_CODE)?.show()
            }
            return false
        }
    }
}

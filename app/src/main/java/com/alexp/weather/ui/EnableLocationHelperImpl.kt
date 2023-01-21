package com.alexp.weather.ui

import android.app.Activity
import android.content.IntentSender
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes

private const val TAG = "EnableLocationHelper"

class EnableLocationHelperImpl: EnableLocationHelper {

    var activity: ComponentActivity? = null
        set(value) {
            field = value
            if (value != null) {
                launcher = getLauncher(value)
            }
        }

    private var launcher: ActivityResultLauncher<IntentSenderRequest>? = null
    private var callback: (Boolean) -> Unit = { }

    override fun enableLocation(callback: (Boolean) -> Unit) {
        this.callback = callback
        val currentActivity = activity
        if (currentActivity == null) {
            Log.d(TAG, "Activity isn't set")
            callback(false)
            return
        }
        val locationRequest = LocationRequest.create()
        locationRequest.apply {
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            interval = 30 * 1000.toLong()
            fastestInterval = 5 * 1000.toLong()
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result = LocationServices.getSettingsClient(currentActivity).checkLocationSettings(builder.build())
        result.addOnCompleteListener {
            try {
                val response: LocationSettingsResponse = it.getResult(ApiException::class.java)
                Log.d(TAG,  "isGpsPresent = ${response.locationSettingsStates?.isGpsPresent}")
                if(response.locationSettingsStates?.isGpsPresent == true) {
                    callback(true)
                }

            } catch (e: ApiException){
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolution = e.status.resolution
                        if (resolution != null) {
                            val intentSenderRequest = IntentSenderRequest.Builder(resolution).build()
                            launcher?.launch(intentSenderRequest)
                        } else {
                            Log.e(TAG, "Resolution is null")
                            callback(false)
                        }
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Enable location error", e)
                    }
                }
            }
        }
    }

    private fun getLauncher(activity: ComponentActivity): ActivityResultLauncher<IntentSenderRequest> {
        return activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Turn on location: SUCCESS")
                callback(true)
            } else {
                Log.d(TAG, "Turn on location: CANCEL")
                callback(true)
            }
        }
    }
}
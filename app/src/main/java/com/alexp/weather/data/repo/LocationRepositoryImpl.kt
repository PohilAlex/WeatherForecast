package com.alexp.weather.data.repo

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LocationRepositoryImpl: LocationRepository {

    var fusedLocationClient: FusedLocationProviderClient? = null

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location? {
        return suspendCoroutine {  continuation ->
            fusedLocationClient?.lastLocation
                ?.addOnSuccessListener { location : Location? ->
                   continuation.resume(location)
                }
                ?.addOnFailureListener { location ->
                    continuation.resumeWithException(location)
                }
                ?.addOnCanceledListener {
                    continuation.resumeWithException(CancellationException("LastLocation task was canceled"))
                }
        }
    }

}
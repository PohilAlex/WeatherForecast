package com.alexp.weather.data.repo

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "LocationRepository"

class LocationRepositoryImpl : LocationRepository {

    var fusedLocationClient: FusedLocationProviderClient? = null

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location? {
        return suspendCoroutine { continuation ->
            fusedLocationClient?.lastLocation
                ?.addOnSuccessListener { location: Location? ->
                    continuation.resume(location)
                }
                ?.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
                ?.addOnCanceledListener {
                    continuation.resumeWithException(CancellationException("LastLocation task was canceled"))
                }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): Location? {
        return suspendCoroutine { continuation ->
            val cancellationToken = null
            fusedLocationClient?.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationToken
            )
                ?.addOnSuccessListener { location: Location? ->
                    continuation.resume(location)
                }
                ?.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
                ?.addOnCanceledListener {
                    continuation.resumeWithException(CancellationException("CurrentLocation task was canceled"))
                }
        }
    }

}
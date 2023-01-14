package com.alexp.weather.data.repo

val LOCATION_PERMISSION = listOf(
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.ACCESS_COARSE_LOCATION
)

interface PermissionRepository {

    fun isPermissionGranted(permissions: List<String>): Boolean
}
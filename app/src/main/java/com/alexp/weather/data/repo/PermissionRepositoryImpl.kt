package com.alexp.weather.data.repo

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import javax.inject.Inject

class PermissionRepositoryImpl @Inject constructor(
    private val app: Application
): PermissionRepository {

    override fun isPermissionGranted(permissions: List<String>): Boolean {
        return permissions
            .map { permission ->
                ContextCompat.checkSelfPermission(
                    app,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }.any { it }
    }
}
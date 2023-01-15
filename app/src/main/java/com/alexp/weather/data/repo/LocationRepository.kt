package com.alexp.weather.data.repo

import android.location.Location

interface LocationRepository {

    //TODO change native Location with a custom one
    suspend fun getLastLocation(): Location?
}
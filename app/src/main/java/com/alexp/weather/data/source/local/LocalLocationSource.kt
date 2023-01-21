package com.alexp.weather.data.source.local

import com.alexp.weather.data.repo.model.WeatherLocation
import kotlinx.coroutines.flow.Flow

interface LocalLocationSource {

    suspend fun storeLocation(data: WeatherLocation)

    fun observeLocation(): Flow<WeatherLocation?>
}
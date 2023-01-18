package com.alexp.weather.data.source.local

import com.alexp.weather.data.source.remote.OneCallRemoteDTO
import kotlinx.coroutines.flow.Flow

interface LocalForecastSource {

    suspend fun storeWeather(data: OneCallRemoteDTO)

    fun observeWhetherUpdate(): Flow<OneCallRemoteDTO?>
}
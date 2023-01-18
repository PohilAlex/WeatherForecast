package com.alexp.weather.data.repo

import com.alexp.weather.data.repo.model.WeatherInfo
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    fun getWeather(): Flow<WeatherInfo?>

    suspend fun refreshWeather(lat: Double, lon: Double)
}
package com.alexp.weather.data.repo

import com.alexp.weather.data.repo.model.WeatherInfo

interface WeatherRepository {

    suspend fun getWeather(): WeatherInfo
}
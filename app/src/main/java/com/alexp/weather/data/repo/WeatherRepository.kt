package com.alexp.weather.data.repo

interface WeatherRepository {

    suspend fun getWeather(): List<DailyWeatherInfo>
}
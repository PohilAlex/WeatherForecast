package com.alexp.weather.data.repo.model

data class HourlyWeatherInfo(
    val dateTime: Long,
    val temp: Double,
    val iconCode: String?,
    val windSpeed: Double
)
package com.alexp.weather.data.repo.model

data class DailyWeatherInfo(
    val dateTime: Long,
    val humidity: Int,
    val tempNight: Double,
    val tempDay: Double,
    val iconCode: String?
)
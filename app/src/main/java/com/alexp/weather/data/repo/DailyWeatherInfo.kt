package com.alexp.weather.data.repo

data class DailyWeatherInfo(
    val dateTime: Long,
    val humidity: Int,
    val tempNight: Double,
    val tempDay: Double,
    val icon: String?
)
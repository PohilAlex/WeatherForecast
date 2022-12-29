package com.alexp.weather.data.repo

data class DailyWeatherInfo(
    val date: Int,
    val tempNight: Double,
    val tempDay: Double,
    val icon: String?
)
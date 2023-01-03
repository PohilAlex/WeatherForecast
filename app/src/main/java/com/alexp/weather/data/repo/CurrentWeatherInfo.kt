package com.alexp.weather.data.repo

data class CurrentWeatherInfo(
    val temp: Int,
    val feelLike: Int,
    val updateTime: Long,
    val icon: String?
)
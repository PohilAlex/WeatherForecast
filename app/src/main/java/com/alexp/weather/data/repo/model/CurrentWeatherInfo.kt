package com.alexp.weather.data.repo.model

data class CurrentWeatherInfo(
    val temp: Int,
    val feelLike: Int,
    val updateTime: Long,
    val iconCode: String?
)
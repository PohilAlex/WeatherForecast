package com.alexp.weather.data.repo.model

data class WeatherInfo(
    val current: CurrentWeatherInfo,
    val daily: List<DailyWeatherInfo>,
    val hourly: List<HourlyWeatherInfo>
)
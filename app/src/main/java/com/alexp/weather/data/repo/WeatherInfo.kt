package com.alexp.weather.data.repo

data class WeatherInfo(
    val current: CurrentWeatherInfo,
    val daily: List<DailyWeatherInfo>,
    val hourly: List<HourlyWeatherInfo>
)
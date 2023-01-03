package com.alexp.weather.ui

data class WeatherUiState(
    val dailyForecast: List<DailyWeatherUiState>
)

data class DailyWeatherUiState(
    val dayName: String,
    val humidity: Int,
    val weatherIcon: String,
    val tempDay: Int,
    val tempNight: Int
)
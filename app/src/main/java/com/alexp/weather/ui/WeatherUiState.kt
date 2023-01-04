package com.alexp.weather.ui

data class WeatherUiState(
    val current: CurrentWeatherUiState,
    val daily: List<DailyWeatherUiState>
)

data class DailyWeatherUiState(
    val dayName: String,
    val humidity: Int,
    val icon: String,
    val tempDay: Int,
    val tempNight: Int
)

data class CurrentWeatherUiState(
    val temp: Int,
    val feelLike: Int,
    val updatedTime: String,
    val icon: String
)
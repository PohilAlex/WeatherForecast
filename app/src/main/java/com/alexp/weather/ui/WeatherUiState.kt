package com.alexp.weather.ui

data class WeatherUiState(
    val current: CurrentWeatherUiState,
    val daily: List<DailyWeatherUiState>,
    val hourly: List<HourlyWeatherUiState>
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

data class HourlyWeatherUiState(
    val time: String,
    val temp: Int,
    val icon: String,
    val windSpeed: Int
)
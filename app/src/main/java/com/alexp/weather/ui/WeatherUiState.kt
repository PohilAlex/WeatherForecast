package com.alexp.weather.ui

data class WeatherUiState(
    val weatherData: AggregatedWeatherUIState?,
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val isPermissionGranted: Boolean,
    val message: String? = null
)

data class AggregatedWeatherUIState(
    val current: CurrentWeatherUiState,
    val daily: List<DailyWeatherUiState>,
    val hourly: List<HourlyWeatherUiState>,
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
    val chartItem: TempChartItem,
    val windSpeed: Int
)

data class TempChartItem(
    val prevHeight: Float?,
    val currentHeight: Float,
    val nextHeight: Float?
)
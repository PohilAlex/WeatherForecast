package com.alexp.weather.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexp.weather.data.repo.DailyWeatherInfo
import com.alexp.weather.data.repo.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(weatherRepository: WeatherRepository): ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState(emptyList()))
    val uiState: StateFlow<WeatherUiState> = _uiState

    init {
        viewModelScope.launch {
            val weather = weatherRepository.getWeather()
            _uiState.value = WeatherUiState(dailyForecast = weather)
        }

    }
}

data class WeatherUiState(
    val dailyForecast: List<DailyWeatherInfo>
)
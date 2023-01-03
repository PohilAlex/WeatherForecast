package com.alexp.weather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alexp.weather.R
import com.alexp.weather.data.repo.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val app: Application,
    private val weatherRepository: WeatherRepository
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(INIT_UI_STATE)
    val uiState: StateFlow<WeatherUiState> = _uiState
    private val dayOfWeekFormatter = SimpleDateFormat("EEEE", Locale.getDefault())

    init {
        viewModelScope.launch {
            val weather = weatherRepository.getWeather()
            val currentState = CurrentWeatherUiState(
                temp = weather.current.temp,
                feelLike = weather.current.feelLike
            )
            val dailyForecast = weather.daily.map { dailyWeatherInfo ->
                DailyWeatherUiState(
                    dayName = getDayOfWeek(dailyWeatherInfo.dateTime),
                    humidity = dailyWeatherInfo.humidity,
                    weatherIcon = getIconUrl(dailyWeatherInfo.icon),
                    tempDay = dailyWeatherInfo.tempDay.roundToInt(),
                    tempNight = dailyWeatherInfo.tempNight.roundToInt()
                )
            }
            _uiState.value = WeatherUiState(
                current = currentState,
                daily = dailyForecast
            )
        }

    }

    private fun getIconUrl(iconCode: String?): String {
        return iconCode?.let {
            "https://openweathermap.org/img/wn/$iconCode@2x.png"
        } ?: ""
    }


    private fun getDayOfWeek(dt: Long): String {
        if (isCurrentDay(dt)) {
            return app.getString(R.string.today)
        }
        return dayOfWeekFormatter.format(Date(dt))
    }

    private fun isCurrentDay(dt: Long): Boolean {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dt
        }
        val currentCalendar = Calendar.getInstance()
        return  calendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR)
    }

}

private val INIT_UI_STATE = WeatherUiState(
    current = CurrentWeatherUiState(temp = 0, feelLike = 0),
    daily = emptyList()
)
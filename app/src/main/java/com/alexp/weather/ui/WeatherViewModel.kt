package com.alexp.weather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alexp.weather.R
import com.alexp.weather.data.repo.CurrentWeatherInfo
import com.alexp.weather.data.repo.WeatherInfo
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
    private val updateTimeFormatter = SimpleDateFormat("EEE, HH:mm", Locale.getDefault())
    private val hourlyTimeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        viewModelScope.launch {
            val weather = weatherRepository.getWeather()
            _uiState.value = WeatherUiState(
                current = currentCurrentWeatherUiState(weather.current),
                daily = dailyDailyWeatherUiStates(weather),
                hourly = getHourlyWeatherUiStates(weather)
            )
        }
    }

    private fun currentCurrentWeatherUiState(current: CurrentWeatherInfo): CurrentWeatherUiState =
        CurrentWeatherUiState(
            temp = current.temp,
            feelLike = current.feelLike,
            updatedTime = updateTimeFormatter.format(Date(current.updateTime)),
            icon = getIconUrl(current.iconCode)
        )

    private fun dailyDailyWeatherUiStates(weather: WeatherInfo): List<DailyWeatherUiState> =
        weather.daily.map { dailyWeatherInfo ->
            DailyWeatherUiState(
                dayName = getDayOfWeek(dailyWeatherInfo.dateTime),
                humidity = dailyWeatherInfo.humidity,
                icon = getIconUrl(dailyWeatherInfo.iconCode),
                tempDay = dailyWeatherInfo.tempDay.roundToInt(),
                tempNight = dailyWeatherInfo.tempNight.roundToInt()
            )
        }

    private fun getHourlyWeatherUiStates(weather: WeatherInfo): List<HourlyWeatherUiState> {
        val hours = weather.hourly.take(24)
        val minTemp = hours.minOf { it.temp }
        val maxTemp = hours.maxOf { it.temp }

        fun getChartHeight(temp: Double): Float {
            return 1 - ((temp - minTemp) / (maxTemp - minTemp)).toFloat()
        }

        val hoursUiState: MutableList<HourlyWeatherUiState> = mutableListOf()
        for (i in hours.indices) {
            val hourlyInfo = hours[i]
            hoursUiState.add(
                HourlyWeatherUiState(
                    time = hourlyTimeFormatter.format(Date(hourlyInfo.dateTime)),
                    temp = hourlyInfo.temp.roundToInt(),
                    icon = getIconUrl(hourlyInfo.iconCode),
                    windSpeed = hourlyInfo.windSpeed.roundToInt(),
                    chartItem = TempChartItem(
                        prevHeight = hours.getOrNull(i - 1)?.let { getChartHeight((it.temp + hourlyInfo.temp) / 2) },
                        currentHeight = getChartHeight(hourlyInfo.temp),
                        nextHeight = hours.getOrNull(i + 1)?.let { getChartHeight((it.temp + hourlyInfo.temp) / 2) }
                    )
                )
            )
        }
       return hoursUiState
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
    current = CurrentWeatherUiState(
        temp = 0,
        feelLike = 0,
        updatedTime = "",
        icon = ""
    ),
    daily = emptyList(),
    hourly = emptyList()
)
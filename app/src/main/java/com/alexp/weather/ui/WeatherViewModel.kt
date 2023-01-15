package com.alexp.weather.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alexp.weather.R
import com.alexp.weather.data.repo.CurrentWeatherInfo
import com.alexp.weather.data.repo.PermissionRepository
import com.alexp.weather.data.repo.WeatherInfo
import com.alexp.weather.data.repo.WeatherRepository
import com.alexp.weather.data.repo.LOCATION_PERMISSION
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

private const val TAG = "WeatherViewModel"
private const val MIN_LOADING_TIME = 1000

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val app: Application,
    private val weatherRepository: WeatherRepository,
    private val permissionRepository: PermissionRepository
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(INIT_UI_STATE)
    val uiState: StateFlow<WeatherUiState> = _uiState

    private val dayOfWeekFormatter = SimpleDateFormat("EEEE", Locale.getDefault())
    private val updateTimeFormatter = SimpleDateFormat("EEE, HH:mm", Locale.getDefault())
    private val hourlyTimeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        viewModelScope.launch {
            val isGranted = permissionRepository.isPermissionGranted(LOCATION_PERMISSION)
            _uiState.value = _uiState.value.copy(
                isPermissionGranted = isGranted
            )
        }
        viewModelScope.launch {
            updateWeather(updateLoaderOnSuccess = true)
        }
    }

    private suspend fun updateWeather(updateLoaderOnSuccess: Boolean) {
        try {
            val weather = weatherRepository.getWeather()
            _uiState.value = _uiState.value.copy(
                weatherData = AggregatedWeatherUIState(
                    current = currentCurrentWeatherUiState(weather.current),
                    daily = dailyDailyWeatherUiStates(weather),
                    hourly = getHourlyWeatherUiStates(weather),
                ),
                isLoading = if (updateLoaderOnSuccess) false else _uiState.value.isLoading
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Load weather failed", e)
            _uiState.value = _uiState.value.copy(
                message = "Something went wrong =(",
                isLoading = false,
            )
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true
            )
            val startTime = System.currentTimeMillis()
            updateWeather(updateLoaderOnSuccess = false)
            val endTime = System.currentTimeMillis()
            val delay = endTime - startTime
            Log.d(TAG, "Refresh time=$delay")
            delay(max(0, MIN_LOADING_TIME - delay))
            _uiState.value = _uiState.value.copy(
                isLoading = false
            )
        }
    }

    fun onMessageShown() {
        _uiState.value = _uiState.value.copy(
            message = null
        )
    }

    fun onLocationPermissionChanged(isGranted: Boolean) {
        _uiState.value = _uiState.value.copy(
            isPermissionGranted = isGranted
        )
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
    weatherData = null,
    isPermissionGranted = false,
    isLoading = true
)
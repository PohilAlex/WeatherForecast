package com.alexp.weather.ui

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alexp.weather.R
import com.alexp.weather.data.repo.LOCATION_PERMISSION
import com.alexp.weather.data.repo.LocationRepository
import com.alexp.weather.data.repo.PermissionRepository
import com.alexp.weather.data.repo.WeatherRepository
import com.alexp.weather.data.repo.model.CurrentWeatherInfo
import com.alexp.weather.data.repo.model.WeatherInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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

    var locationRepository: LocationRepository? = null
        set(value) {
            field = value
            retrieveLocationAndUpdateWeather()
        }

    val uiState: StateFlow<WeatherUiState>
    private val _isLoading = MutableStateFlow(INIT_UI_STATE.isLoading)
    private val _isRefreshing = MutableStateFlow(INIT_UI_STATE.isRefreshing)
    private val _isLocationPermissionGranted = MutableStateFlow(INIT_UI_STATE.isPermissionGranted)
    private val _userMessage: MutableStateFlow<String?> = MutableStateFlow(null)

    private val dayOfWeekFormatter = SimpleDateFormat("EEEE", Locale.getDefault())
    private val updateTimeFormatter = SimpleDateFormat("EEE, HH:mm", Locale.getDefault())
    private val hourlyTimeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        viewModelScope.launch {
            val isGranted = permissionRepository.isPermissionGranted(LOCATION_PERMISSION)
            _isLocationPermissionGranted.value = isGranted
            retrieveLocationAndUpdateWeather()
        }
    }

    private fun getWeather(): Flow<WeatherInfo?> {
        return weatherRepository.getWeather()
            .onStart {
                emit(null)
            }
            .catch {
                Log.e(TAG, "Error while loading data", it)
            }
    }

    private fun retrieveLocationAndUpdateWeather(isForce: Boolean = false) {
        if (locationRepository != null && _isLocationPermissionGranted.value) {
            viewModelScope.launch {
                var location: Location? = null
                try {
                    location = locationRepository?.getLastLocation()
                } catch (ex: CancellationException) {
                    throw ex
                } catch (ex: Exception) {
                    Log.e(TAG, "getLastLocation exception", ex)
                }

                Log.d(TAG, "last location=$location")
                //TODO handle case when location is null
                if (location != null) {
                    updateWeather(
                        isForce = isForce,
                        lat = location.latitude,
                        lon = location.longitude
                    )
                }
            }
        }
    }

    private suspend fun updateWeather(isForce: Boolean, lat: Double, lon: Double) {
        try {
            _isLoading.value = true
            weatherRepository.refreshWeather(lat = lat, lon = lon)
            _isLoading.value = false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Load weather failed", e)
            _userMessage.value = "Something went wrong =("
            _isLoading.value = false
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val startTime = System.currentTimeMillis()
            retrieveLocationAndUpdateWeather(isForce = true)
            val endTime = System.currentTimeMillis()
            val delay = endTime - startTime
            Log.d(TAG, "Refresh time=$delay")
            delay(max(0, MIN_LOADING_TIME - delay))
            _isRefreshing.value = false
        }
    }

    fun onMessageShown() {
        _userMessage.value = null
    }

    fun onLocationPermissionChanged(isGranted: Boolean) {
        _isLocationPermissionGranted.value = isGranted
        if (isGranted) {
            retrieveLocationAndUpdateWeather()
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
    weatherData = null,
    isPermissionGranted = true,
    isLoading = true,
    isRefreshing = false
)
package com.alexp.weather.data.repo

import android.util.Log
import com.alexp.weather.data.repo.model.CurrentWeatherInfo
import com.alexp.weather.data.repo.model.DailyWeatherInfo
import com.alexp.weather.data.repo.model.HourlyWeatherInfo
import com.alexp.weather.data.repo.model.WeatherInfo
import com.alexp.weather.data.source.local.LocalForecastSource
import com.alexp.weather.data.source.remote.WeatherApi
import com.alexp.weather.data.source.remote.WeatherItemRemoteDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.roundToInt

private const val TAG = "WeatherRepository"

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi,
    private val localForecastSource: LocalForecastSource
) : WeatherRepository {

    override fun getWeather(): Flow<WeatherInfo?> {
        return localForecastSource.observeWhetherUpdate()
            .map { remoteData ->
                if (remoteData == null) return@map null
                val current = remoteData.current
                WeatherInfo(
                    current = CurrentWeatherInfo(
                        temp = current.temp.roundToInt(),
                        feelLike = current.feelsLike.roundToInt(),
                        updateTime = current.dt.toMillis(),
                        iconCode = current.weather.getIconCode()
                    ),
                    daily = remoteData.daily.map { remoteDaily ->
                        DailyWeatherInfo(
                            dateTime = remoteDaily.dt.toMillis(),
                            humidity = remoteDaily.humidity,
                            tempNight = remoteDaily.temp.night,
                            tempDay = remoteDaily.temp.day,
                            iconCode = remoteDaily.weather.getIconCode()
                        )
                    },
                    hourly = remoteData.hourly.map { remoteHourly ->
                        HourlyWeatherInfo(
                            dateTime = remoteHourly.dt.toMillis(),
                            temp = remoteHourly.temp,
                            iconCode = remoteHourly.weather.getIconCode(),
                            windSpeed = remoteHourly.windSpeed
                        )
                    }
                )
            }
    }

    override suspend  fun refreshWeather(lat: Double, lon: Double) {
        Log.d(TAG, "#refreshWeather")
        val remoteData = weatherApi.getOneCallData(
            lat = lat,
            lon = lon,
        )
        Log.d(TAG, "New weather data=$remoteData")
        localForecastSource.storeWeather(remoteData)
    }
}

private fun Int.toMillis(): Long = this * 1000L

private fun List<WeatherItemRemoteDTO>.getIconCode(): String? = firstOrNull()?.icon
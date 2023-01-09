package com.alexp.weather.data.repo

import android.util.Log
import com.alexp.weather.data.source.remote.WeatherApi
import com.alexp.weather.data.source.remote.WeatherItemRemoteDTO
import javax.inject.Inject
import kotlin.math.roundToInt

private const val TAG = "WeatherRepository"

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi
) : WeatherRepository {

    override suspend fun getWeather(): WeatherInfo {
        val remoteData = weatherApi.getOneCallData()
        Log.d(TAG, "Weather updated data=$remoteData")
        val current = remoteData.current
        return WeatherInfo(
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

private fun Int.toMillis(): Long = this * 1000L

private fun List<WeatherItemRemoteDTO>.getIconCode(): String? = firstOrNull()?.icon
package com.alexp.weather.data.repo

import com.alexp.weather.data.source.remote.WeatherApi
import javax.inject.Inject
import kotlin.math.roundToInt

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi
) : WeatherRepository {

    override suspend fun getWeather(): WeatherInfo {
        val remoteData = weatherApi.getOneCallData()
        val current = remoteData.current
        return WeatherInfo(
            current = CurrentWeatherInfo(
                temp = current.temp.roundToInt(),
                feelLike = current.feelsLike.roundToInt(),
                updateTime = current.dt.toMillis(),
                iconCode = current.weather.firstOrNull()?.icon
            ),
            daily = remoteData.daily.map { remoteDaily ->
                DailyWeatherInfo(
                    dateTime = remoteDaily.dt.toMillis(),
                    humidity = remoteDaily.humidity,
                    tempNight = remoteDaily.temp.night,
                    tempDay = remoteDaily.temp.day,
                    icon = remoteDaily.weather.firstOrNull()?.icon
                )
            }
        )
    }
}

fun Int.toMillis(): Long = this * 1000L
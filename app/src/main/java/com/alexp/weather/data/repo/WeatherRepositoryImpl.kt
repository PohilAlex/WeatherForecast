package com.alexp.weather.data.repo

import com.alexp.weather.data.source.remote.WeatherApi
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi
) : WeatherRepository {

    override suspend fun getWeather(): List<DailyWeatherInfo> {
        val remoteData = weatherApi.getOneCallData()
        return remoteData.daily.map { remoteDaily ->
            DailyWeatherInfo(
                dateTime = remoteDaily.dt.toMillis(),
                humidity = remoteDaily.humidity,
                tempNight = remoteDaily.temp.night,
                tempDay = remoteDaily.temp.day,
                icon = remoteDaily.weather.firstOrNull()?.icon
            )
        }
    }
}

fun Int.toMillis(): Long = this * 1000L
package com.alexp.weather.data.source.remote

import com.alexp.weather.data.RemoteOneCallData
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://api.openweathermap.org"

interface WeatherApi {

    @GET("data/3.0/onecall")
    suspend fun getOneCallData(
        @Query("lat") lat: Double = 50.450001,
        @Query("lon") lon: Double = 30.523333,
        @Query("exclude") exclude: String = "minutely,hourly,alerts",
        @Query("units") unit: String = "metric",
        @Query("appid") appid: String = "96c36fd2d716d9538d725e92c7b2ffa3"
    ): RemoteOneCallData

}
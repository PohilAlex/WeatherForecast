package com.alexp.weather.data.source.remote

import com.google.gson.annotations.SerializedName

data class OneCallRemoteDTO(
    @SerializedName("current")
    val current: CurrentRemoteDTO,
    @SerializedName("daily")
    val daily: List<DailyRemoteDTO>,
    @SerializedName("hourly")
    val hourly: List<HourlyRemoteDTO>
)

data class WeatherItemRemoteDTO(
    @SerializedName("description")
    val description: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("main")
    val main: String
)


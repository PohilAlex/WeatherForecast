package com.alexp.weather.data.repo.model

import com.google.gson.annotations.SerializedName

data class WeatherLocation(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double
)
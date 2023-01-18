package com.alexp.weather.data.source.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alexp.weather.data.source.remote.OneCallRemoteDTO
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val TAG = "LocalSource"

class LocalForecastSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
): LocalForecastSource {

    private val gson: Gson = Gson()
    private val forecastKey = stringPreferencesKey("forecast")

    override suspend fun storeWeather(data: OneCallRemoteDTO) {
        Log.d(TAG, "#storeWeather")
        gson.toJson(data)
        dataStore.edit { store ->
            store[forecastKey] = gson.toJson(data)
        }
    }

    override fun observeWhetherUpdate(): Flow<OneCallRemoteDTO?> {
        return dataStore.data.map { preferences ->
            Log.d(TAG, "#observeWhetherUpdate start")
            val forecastData = preferences[forecastKey]
            val dto = gson.fromJson(forecastData, OneCallRemoteDTO::class.java)
            Log.d(TAG, "#observeWhetherUpdate newValue=$dto")
            dto
        }
    }
}
package com.alexp.weather.data.source.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alexp.weather.data.repo.model.WeatherLocation
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val TAG = "LocalLocationSource"

class LocalLocationSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
): LocalLocationSource {
    private val gson: Gson = Gson()
    private val location = stringPreferencesKey("location")


    override suspend fun storeLocation(data: WeatherLocation) {
        Log.d(TAG, "#storeLocation: $data")
        gson.toJson(data)
        dataStore.edit { store ->
            store[location] = gson.toJson(data)
        }
    }

    //TODO measure the DataSource performance.
    override fun observeLocation(): Flow<WeatherLocation?> {
        return dataStore.data.map { preferences ->
            val forecastData = preferences[location]
            val location = gson.fromJson(forecastData, WeatherLocation::class.java)
            Log.d(TAG, "#observeLocation value=$location")
            location
        }
    }
}
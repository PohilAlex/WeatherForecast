package com.alexp.weather.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.alexp.weather.data.repo.PermissionRepository
import com.alexp.weather.data.repo.PermissionRepositoryImpl
import com.alexp.weather.data.repo.WeatherRepository
import com.alexp.weather.data.repo.WeatherRepositoryImpl
import com.alexp.weather.data.source.local.LocalForecastSource
import com.alexp.weather.data.source.local.LocalForecastSourceImpl
import com.alexp.weather.data.source.local.LocalLocationSource
import com.alexp.weather.data.source.local.LocalLocationSourceImpl
import com.alexp.weather.data.source.remote.BASE_URL
import com.alexp.weather.data.source.remote.WeatherApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providesWeatherApi(retrofit: Retrofit): WeatherApi {
        return retrofit.create(WeatherApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "forecast"
    )

    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.userPreferencesDataStore
    }

    @Provides
    fun getLocalForecastSource(source: LocalForecastSourceImpl): LocalForecastSource = source

    @Provides
    fun getLocalLocationSource(source: LocalLocationSourceImpl): LocalLocationSource = source
}

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Provides
    fun getWeatherRepository(repo: WeatherRepositoryImpl): WeatherRepository = repo

    @Provides
    fun getWeatherModule(repo: PermissionRepositoryImpl): PermissionRepository = repo
}
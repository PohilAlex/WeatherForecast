package com.alexp.weather.di

import com.alexp.weather.data.repo.PermissionRepository
import com.alexp.weather.data.repo.PermissionRepositoryImpl
import com.alexp.weather.data.repo.WeatherRepository
import com.alexp.weather.data.repo.WeatherRepositoryImpl
import com.alexp.weather.data.source.remote.BASE_URL
import com.alexp.weather.data.source.remote.WeatherApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
object RepoModule {

    @Provides
    fun getWeatherRepository(repo: WeatherRepositoryImpl): WeatherRepository = repo

    @Provides
    fun getWeatherModule(repo: PermissionRepositoryImpl): PermissionRepository = repo
}
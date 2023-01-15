package com.alexp.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.alexp.weather.data.repo.LocationRepositoryImpl
import com.alexp.weather.ui.WeatherScreen
import com.alexp.weather.ui.WeatherViewModel
import com.alexp.weather.ui.theme.WeatherForecastTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationRepository = LocationRepositoryImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRepository.fusedLocationClient = fusedLocationClient
        viewModel.locationRepository = locationRepository
        setContent {
            WeatherForecastTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(Modifier.fillMaxSize()) {
                        WeatherScreen(viewModel)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationRepository.fusedLocationClient = null
    }
}

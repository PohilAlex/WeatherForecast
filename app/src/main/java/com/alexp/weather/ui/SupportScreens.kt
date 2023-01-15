package com.alexp.weather.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexp.weather.R
import com.alexp.weather.data.repo.LOCATION_PERMISSION
import com.alexp.weather.ui.theme.Background
import com.alexp.weather.ui.theme.HumidityHigh
import com.alexp.weather.ui.theme.WeatherForecastTheme

private const val TAG = "WeatherScreen"

@Composable
fun PermissionNotGrantedView(
    onLocationPermissionChanged: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGrantedMap: Map<String, Boolean> ->
            Log.d(TAG, "isPermissionGrantedMap=$isGrantedMap")
            val isGranted = isGrantedMap.any { it.value }
            if (isGranted) {
                onLocationPermissionChanged(true)
            } else {
                onLocationPermissionChanged(false)
            }
        }

        Column(modifier = Modifier.align(Alignment.Center)) {
            Text(
                text = stringResource(R.string.ask_location_permission),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 16.dp)
            )
            Button(
                onClick = {
                    Log.d(TAG, "Requesting Permission...")
                    launcher.launch(LOCATION_PERMISSION.toTypedArray())
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = HumidityHigh,
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.allow_permission))
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            Icon(
                painterResource(id = R.drawable.ic_loading_loading),
                contentDescription = null,
                tint = HumidityHigh,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(36.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(R.string.loading),
                fontSize = 18.sp,
            )
        }
    }
}

@Composable
fun RetryView(
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            Icon(
                painterResource(id = R.drawable.ic_retry),
                contentDescription = null,
                tint = HumidityHigh,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(36.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(R.string.data_not_available),
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = HumidityHigh,
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.regresh))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionNotGrantedViewPreview() {
    WeatherForecastTheme {
        PermissionNotGrantedView(onLocationPermissionChanged = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyViewPreview() {
    WeatherForecastTheme {
        LoadingView()
    }
}

@Preview(showBackground = true)
@Composable
private fun RetryPreview() {
    WeatherForecastTheme {
        RetryView(onRetry = { })
    }
}
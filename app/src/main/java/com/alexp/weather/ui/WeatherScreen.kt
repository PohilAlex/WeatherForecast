package com.alexp.weather.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.alexp.weather.R
import com.alexp.weather.ui.theme.Background
import com.alexp.weather.ui.theme.HumidityHigh
import com.alexp.weather.ui.theme.HumidityLow
import com.alexp.weather.ui.theme.Shapes
import com.alexp.weather.ui.theme.WeatherForecastTheme


@Composable
fun WeatherScreen(forecastState: WeatherUiState) {
    Column(modifier = Modifier.background(Background)) {
        CurrentWeather(forecastState.current)
        DailyForecast(forecastState.daily)
    }
}

@Composable
private fun CurrentWeather(current: CurrentWeatherUiState) {
    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp).fillMaxWidth()) {
        Column {
            Text(
                text = "${current.temp}°",
                fontSize = 72.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Text(
                text = stringResource(R.string.feel_like, current.feelLike),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = current.updatedTime,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        AsyncImage(
            model = current.icon,
            contentDescription = null,
            modifier = Modifier
                .padding(top = 6.dp)
                .size(90.dp)
                .align(Alignment.TopEnd)
        )
    }

}

@Composable
private fun DailyForecast(dailyForecast: List<DailyWeatherUiState>) {
    Surface(
        elevation = 8.dp,
        shape = Shapes.medium,
        modifier = Modifier.padding(10.dp)
    ) {
        Column {
            for (day in dailyForecast) {
                DailyItemForecast(day)
            }
        }
    }

}

@Composable
private fun DailyItemForecast(day: DailyWeatherUiState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = day.dayName,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .requiredWidth(100.dp)
        )
        Spacer(modifier = Modifier.weight(2f))
        Image(
            painter = painterResource(id = R.drawable.ic_humidity),
            contentDescription = null,
            colorFilter = ColorFilter.tint(if (day.humidity >= 50) HumidityHigh else HumidityLow),
            alignment = Alignment.Center,
            modifier = Modifier
                .size(12.dp)
                .padding(end = 2.dp)
        )
        Text(
            fontSize = 12.sp,
            maxLines = 1,
            text = "${day.humidity}%", modifier = Modifier
                .requiredWidth(40.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        AsyncImage(
            model = day.icon,
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .padding(horizontal = 6.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${day.tempDay}°",
            textAlign = TextAlign.End,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .requiredWidth(40.dp)
        )
        Text(
            text = "${day.tempNight}°",
            textAlign = TextAlign.End,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .requiredWidth(40.dp)

        )
    }
}


@Preview(showBackground = true)
@Composable
private fun CurrentWeatherPreview() {
    WeatherForecastTheme {
        WeatherScreen(
            WeatherUiState(
                current = CurrentWeatherUiState(
                    temp = 10,
                    feelLike = 12,
                    updatedTime = "Wed, 11:28",
                    icon = ""
                ),
                daily = listOf(
                    DailyWeatherUiState(
                        dayName = "Today",
                        humidity = 10,
                        icon = "",
                        tempDay = 5,
                        tempNight = 20
                    )
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherScreenPreview() {
    WeatherForecastTheme {
        CurrentWeather(
            CurrentWeatherUiState(
                temp = 10,
                feelLike = 12,
                updatedTime = "Wed, 11:28",
                icon = ""
            )
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun DailyItemForecastPreview() {
    WeatherForecastTheme {
        DailyItemForecast(
            DailyWeatherUiState(
                dayName = "Today",
                humidity = 10,
                icon = "https://openweathermap.org/img/wn/10d@2x.png",
                tempDay = 5,
                tempNight = 20
            )
        )
    }
}
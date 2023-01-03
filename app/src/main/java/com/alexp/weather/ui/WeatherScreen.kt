package com.alexp.weather.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.alexp.weather.R
import com.alexp.weather.ui.theme.HumidityHigh
import com.alexp.weather.ui.theme.HumidityLow
import com.alexp.weather.ui.theme.WeatherForecastTheme


@Composable
fun WeatherScreen(forecastState: WeatherUiState) {
    DailyForecast(forecastState.daily)
}

@Composable
private fun DailyForecast(dailyForecast: List<DailyWeatherUiState>) {
    Column {
        for (day in dailyForecast) {
            DailyItemForecast(day)
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
            model = day.weatherIcon,
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
            //maxLines = 1,
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
fun DefaultPreview() {
    WeatherForecastTheme {
        DailyItemForecast(
            DailyWeatherUiState(
                dayName = "Today",
                humidity = 10,
                weatherIcon = "https://openweathermap.org/img/wn/10d@2x.png",
                tempDay = 5,
                tempNight = 20
            )
        )
    }
}
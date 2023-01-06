package com.alexp.weather.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.alexp.weather.R
import com.alexp.weather.ui.theme.Background
import com.alexp.weather.ui.theme.HumidityHigh
import com.alexp.weather.ui.theme.HumidityLow
import com.alexp.weather.ui.theme.Shapes
import com.alexp.weather.ui.theme.WeatherForecastTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min


private val toolbarHeightMax = 170.dp
private val toolbarHeightMin = 100.dp

@Composable
fun WeatherScreen(forecastState: WeatherUiState) {

    val toolbarHeightMaxPx = with(LocalDensity.current) { toolbarHeightMax.roundToPx().toFloat() }
    val toolbarHeightMinPx = with(LocalDensity.current) { toolbarHeightMin.roundToPx().toFloat() }
    val toolbarExpandRatio = remember { mutableStateOf(1f) }
    val coroutineScope = rememberCoroutineScope()

    val nestedScrollConnection = remember {
        nestedScrollConnection(
            toolbarExpandRatio,
            toolbarHeightMaxPx,
            toolbarHeightMinPx,
            coroutineScope
        )
    }

    Column(modifier = Modifier
        .background(Background)
        .nestedScroll(nestedScrollConnection)) {
        CurrentWeather(forecastState.current, toolbarExpandRatio.value)
        LazyColumn {
            item { DailyForecast(forecastState.daily) }
        }
    }
}

@Composable
private fun CurrentWeather(current: CurrentWeatherUiState, expandRatio: Float) {
    Box(
        modifier = Modifier
            .padding(start = 24.dp, end = 12.dp, top = 24.dp)
            .fillMaxWidth()
            .height(toolbarHeightMin + (toolbarHeightMax - toolbarHeightMin) * expandRatio)
    ) {
            Text(
                text = "${current.temp}°",
                fontSize = 72.sp,
            )
            Text(
                text = stringResource(R.string.feel_like, current.feelLike),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 70.dp * expandRatio + 35.dp, start = 80.dp * (1 - expandRatio))
            )
            Text(
                text = current.updatedTime,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 70.dp * expandRatio + 55.dp, start = 80.dp * (1 - expandRatio))
            )
        AsyncImage(
            model = current.icon,
            contentDescription = null,
            modifier = Modifier
                .padding(top = 6.dp)
                .size(90.dp + 40.dp * expandRatio)
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

private fun nestedScrollConnection(
    toolbarExpandRatio: MutableState<Float>,
    toolbarHeightMaxPx: Float,
    toolbarHeightMinPx: Float,
    coroutineScope: CoroutineScope
) = object : NestedScrollConnection {

    private val iterationCount = 10
    private val animationTime = 100L

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (available.y > 0) {
            //down
            return if (getCurrentToolbarHeightPx() < toolbarHeightMaxPx) {
                val newHeight = getCurrentToolbarHeightPx() + available.y
                toolbarExpandRatio.value = (min(
                    newHeight,
                    toolbarHeightMaxPx
                ) - toolbarHeightMinPx) / (toolbarHeightMaxPx - toolbarHeightMinPx)
                val overscroll = max(newHeight - toolbarHeightMaxPx, 0f)
                Offset(0f, available.y + overscroll)
            } else {
                Offset.Zero
            }
        } else {
            //up
            return if (getCurrentToolbarHeightPx() > toolbarHeightMinPx) {
                val newHeight = getCurrentToolbarHeightPx() + available.y
                toolbarExpandRatio.value = (max(
                    newHeight,
                    toolbarHeightMinPx
                ) - toolbarHeightMinPx) / (toolbarHeightMaxPx - toolbarHeightMinPx)
                val overscroll = max(toolbarHeightMinPx - newHeight, 0f)
                Offset(0f, available.y + overscroll)
            } else {
                Offset.Zero
            }
        }
    }

    fun getCurrentToolbarHeightPx(): Float =
        toolbarHeightMaxPx * toolbarExpandRatio.value + toolbarHeightMinPx * (1 - toolbarExpandRatio.value)


    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val step = if (toolbarExpandRatio.value > 0.5) {
            (1 - toolbarExpandRatio.value) / iterationCount
        } else {
            -toolbarExpandRatio.value / iterationCount
        }
        coroutineScope.launch {
            (0..iterationCount)
                .asSequence()
                .asFlow()
                .onEach {
                    var newRatio = toolbarExpandRatio.value + step
                    if (newRatio > 1) {
                        newRatio = 1f
                    }
                    if (newRatio < 0) {
                        newRatio = 0f
                    }
                    toolbarExpandRatio.value = newRatio
                    delay(animationTime / iterationCount)
                }
                .collect()
        }
        return super.onPostFling(consumed, available)
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
                ),
                hourly = emptyList()
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
            ),
            expandRatio = 1f
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
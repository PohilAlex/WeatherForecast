package com.alexp.weather.ui

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.alexp.weather.R
import com.alexp.weather.ui.theme.Background
import com.alexp.weather.ui.theme.ChartGrey
import com.alexp.weather.ui.theme.Grey
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
private const val TAG = "WeatherScreen"

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    val forecastState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(scaffoldState = scaffoldState) { paddingValues ->
        val weatherData = forecastState.weatherData
        if (weatherData == null) {
            if (!forecastState.isPermissionGranted) {
                PermissionNotGrantedView(
                    onLocationPermissionChanged = { viewModel.onLocationPermissionChanged(it) }
                )
            } else if (!forecastState.isLocationAvailable) {
                EnableLocationView(onEnableLocation = { viewModel.onEnableLocation()})
            } else if (forecastState.isLoading) {
                LoadingView()
            } else {
                RetryView(onRetry = { viewModel.onRefresh() })
            }
        } else {
            WeatherContent(
                weatherData = weatherData,
                isRefreshing = forecastState.isRefreshing,
                onRefresh = { viewModel.onRefresh() },
                modifier = Modifier.padding(paddingValues)
            )
        }
        forecastState.message?.let { message ->
            LaunchedEffect(message, scaffoldState) {
                scaffoldState.snackbarHostState.showSnackbar(message)
                viewModel.onMessageShown()
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun WeatherContent(
    weatherData: AggregatedWeatherUIState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier
) {
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

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh,
        refreshingOffset = 80.dp
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .nestedScroll(nestedScrollConnection)
        ) {
            CurrentWeather(weatherData.current, toolbarExpandRatio.value)
            CompositionLocalProvider(
                LocalOverscrollConfiguration provides null
            ) {
                LazyColumn {
                    item { HourlyForecast(weatherData.hourly) }
                    item { DailyForecast(weatherData.daily) }
                }
            }
        }
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
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
        val localDensity = LocalDensity.current
        val tempWidthDp = remember { mutableStateOf(0.dp) }
        Text(
            text = "${current.temp}°",
            fontSize = 72.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.onGloballyPositioned {
                tempWidthDp.value = with(localDensity) { it.size.width.toDp() }
            }
        )
        Text(
            text = stringResource(R.string.feel_like, current.feelLike),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                top = 70.dp * expandRatio + 35.dp,
                start = (tempWidthDp.value + 10.dp) * (1 - expandRatio)
            )
        )
        Text(
            text = current.updatedTime,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                top = 70.dp * expandRatio + 55.dp,
                start = (tempWidthDp.value + 10.dp) * (1 - expandRatio)
            )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HourlyForecast(hourly: List<HourlyWeatherUiState>) {
    Surface(
        elevation = 8.dp,
        shape = Shapes.medium,
        modifier = Modifier.padding(10.dp)
    ) {
        CompositionLocalProvider(
            LocalOverscrollConfiguration provides null
        ) {
            LazyRow(modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)) {
                for (hour in hourly) {
                    item { HourItemForecast(hour) }
                }
            }
        }
    }
}

@Composable
private fun HourItemForecast(hour: HourlyWeatherUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(50.dp),
    ) {
        val localDensity = LocalDensity.current
        Text(
            text = hour.time,
            fontSize = 12.sp,
            color = Grey,
            fontWeight = FontWeight.SemiBold
        )
        AsyncImage(
            model = hour.icon,
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
        )
        Text(
            text = "${hour.temp}°",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        TempChartItem(hour, localDensity)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.wind_icon),
                contentDescription = null,
                tint = HumidityHigh,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = hour.windSpeed.toString(),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun TempChartItem(
    hour: HourlyWeatherUiState,
    localDensity: Density
) {
    Canvas(
        modifier = Modifier
            .padding(top = 10.dp)
            .height(height = 35.dp)
            .fillMaxWidth()
    ) {
        val chartItem = hour.chartItem
        drawCircle(
            color = ChartGrey,
            center = Offset(
                x = size.width / 2,
                y = size.height * chartItem.currentHeight
            ),
            radius = with(localDensity) { 3.dp.toPx() }
        )
        if (chartItem.prevHeight != null) {
            drawLine(
                color = ChartGrey,
                strokeWidth = with(localDensity) { 1.dp.toPx() },
                start = Offset(
                    x = 0f,
                    y = size.height * chartItem.prevHeight,
                ),
                end = Offset(
                    x = size.width / 2,
                    y = size.height * chartItem.currentHeight,
                )
            )
        }
        if (chartItem.nextHeight != null) {
            drawLine(
                color = ChartGrey,
                strokeWidth = with(localDensity) { 1.dp.toPx() },
                start = Offset(
                    x = size.width / 2,
                    y = size.height * chartItem.currentHeight,
                ),
                end = Offset(
                    x = size.width,
                    y = size.height * chartItem.nextHeight
                )
            )
        }
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
                .padding(horizontal = 6.dp)
                .size(36.dp)
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
        WeatherContent(
            weatherData = AggregatedWeatherUIState(
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
                hourly = listOf(
                    HourlyWeatherUiState(
                        time = "12:00",
                        temp = 10,
                        icon = "",
                        windSpeed = 3,
                        chartItem = TempChartItem(
                            prevHeight = 0.3f,
                            currentHeight = 0.5f,
                            nextHeight = 0.6f
                        )
                    )
                )
            ),
            isRefreshing = false,
            onRefresh = { },
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherScreenPreview() {
    WeatherForecastTheme {
        CurrentWeather(
            CurrentWeatherUiState(
                temp = -10,
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
private fun HourItemForecastPreview() {
    WeatherForecastTheme {
        HourItemForecast(
            HourlyWeatherUiState(
                time = "12:00",
                temp = 10,
                icon = "",
                windSpeed = 3,
                chartItem = TempChartItem(
                    prevHeight = 0.3f,
                    currentHeight = 0.5f,
                    nextHeight = 0.6f
                )
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
package ru.snowadv.kinopoiskfeaturedmovies.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.snowadv.kinopoiskfeaturedmovies.feat.util.UiEvent
import ru.snowadv.comapr.presentation.EventAggregator
import ru.snowadv.kinopoiskfeaturedmovies.R
import ru.snowadv.kinopoiskfeaturedmovies.feat.util.MainScreen
import ru.snowadv.kinopoiskfeaturedmovies.feat.util.NavigationEvent
import ru.snowadv.kinopoiskfeaturedmovies.presentation.ui.film.info.FilmInfoScreen
import ru.snowadv.kinopoiskfeaturedmovies.presentation.ui.film.list.SearchFilmScreen
import ru.snowadv.kinopoiskfeaturedmovies.presentation.ui.home.HomeScreen
import ru.snowadv.kinopoiskfeaturedmovies.presentation.ui.theme.KinopoiskFeaturedMoviesTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var eventAggregator: EventAggregator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val navController = rememberNavController()

            val navBackStackEntry = navController.currentBackStackEntryAsState()

            LaunchedEffect(true) {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    eventAggregator.eventChannel.receiveAsFlow().onEach {
                        when (it) {
                            is UiEvent.ShowSnackbar -> {
                                snackbarHostState.showSnackbar(it.message)
                            }
                        }
                    }.launchIn(this)

                    eventAggregator.navigationChannel.receiveAsFlow().onEach {
                        when (it) {
                            is NavigationEvent.ToHome -> {
                                navController.navigate(it.route) {
                                    popUpTo(navController.graph.startDestinationRoute!!) {
                                        inclusive = true
                                        saveState = true
                                    }
                                    restoreState = true
                                    launchSingleTop = true
                                }
                            }

                            is NavigationEvent.ToFilmInfo -> {
                                navController.navigate(it.route)
                            }
                        }
                    }.launchIn(this)
                }
            }

            KinopoiskFeaturedMoviesTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.let {
                                if (navBackStackEntry.value?.destination?.route == MainScreen.Home.route) it.padding(
                                    bottom = 80.dp
                                ) else it
                            }.fillMaxWidth()
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavHost(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(it),
                        navController = navController,
                        startDestination = MainScreen.Home.route
                    ) {
                        composable(
                            route = MainScreen.FilmInfo.route,
                            arguments = listOf(
                                navArgument("id") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) {
                            FilmInfoScreen(
                                modifier = Modifier.fillMaxSize(),
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(MainScreen.Home.route) {
                            HomeScreen(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KinopoiskFeaturedMoviesTheme {

    }
}


package org.dhis2.usescases.main.ui.screens

import android.view.LayoutInflater
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.Flow
import org.dhis2.R
import org.dhis2.databinding.ActivityMainBinding
import org.dhis2.mobile.commons.extensions.ObserveAsEvents
import org.dhis2.mobile.login.pin.domain.model.PinMode
import org.dhis2.mobile.login.pin.ui.components.PinDialog
import org.dhis2.usescases.main.HomeScreen
import org.dhis2.usescases.main.MainScreenType
import org.dhis2.usescases.main.ui.model.HomeAction
import org.dhis2.usescases.main.ui.model.HomeEffect
import org.dhis2.usescases.main.ui.model.HomeScreenState
import org.dhis2.utils.customviews.navigationbar.NavigationPage
import org.dhis2.utils.getOnToolbarColor
import org.dhis2.utils.getThemePrimaryColor
import org.hisp.dhis.mobile.ui.designsystem.component.Badge
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.TopBar
import org.hisp.dhis.mobile.ui.designsystem.component.TopBarActionIcon
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar

@Composable
fun MainScreen(
    screenState: HomeScreenState,
    effects: Flow<HomeEffect>,
    onAction: (HomeAction) -> Unit,
    onEffect: (HomeEffect) -> Unit,
    onNewState: (HomeScreenState) -> Unit,
    onNewScreen: (MainScreenType) -> Unit,
    onLayoutInflated: (ActivityMainBinding) -> Unit,
) {
    var showPinDialog by remember { mutableStateOf(false) }

    BackHandler { onAction(HomeAction.BackPressed) }

    ObserveAsEvents(effects) { effect ->
        if (effect is HomeEffect.ShowPinDialog) {
            showPinDialog = true
        } else {
            onEffect(effect)
        }
    }

    LaunchedEffect(screenState) {
        onNewState(screenState)
    }
    LaunchedEffect(screenState.currentScreen) {
        onNewScreen(screenState.currentScreen)
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                screenState = screenState,
                onMenuClicked = { onAction(HomeAction.MenuClicked) },
                onSyncClicked = { onAction(HomeAction.SyncClicked) },
                onFilterClicked = { onAction(HomeAction.FilterClicked) },
            )
        },
        bottomBar = {
            if (screenState.bottomNavigationBarVisible) {
                HomeBottomBar(screenState) { navigationPage ->
                    when (navigationPage) {
                        NavigationPage.ANALYTICS -> onAction(HomeAction.ScreenChanged(MainScreenType.Home(HomeScreen.Visualizations)))
                        NavigationPage.PROGRAMS -> onAction(HomeAction.ScreenChanged(MainScreenType.Home(HomeScreen.Programs)))
                        else -> { /*no op*/ }
                    }
                }
            }
        },
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            factory = { context ->
                ActivityMainBinding
                    .inflate(LayoutInflater.from(context))
                    .also { onLayoutInflated(it) }
                    .root
            },
        )
        if (showPinDialog) {
            PinDialog(
                mode = PinMode.SET,
                onSuccess = {
                    onAction(HomeAction.PinSet)
                    showPinDialog = false
                },
                onDismiss = { showPinDialog = false },
            )
        }
    }
}

@Composable
fun HomeTopBar(
    screenState: HomeScreenState,
    onMenuClicked: () -> Unit,
    onSyncClicked: () -> Unit,
    onFilterClicked: () -> Unit,
) {
    val primaryColor = getThemePrimaryColor()
    val onToolbarColor = getOnToolbarColor()
    TopBar(
        navigationIcon = {
            TopBarActionIcon(
                icon = Icons.Filled.Menu,
                tint = onToolbarColor,
                contentDescription = "menu",
                onClick = onMenuClicked,
            )
        },
        actions = {
            AnimatedVisibility(visible = screenState.syncButtonVisible) {
                IconButton(
                    onClick = onSyncClicked,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = "Sync",
                            tint = onToolbarColor,
                        )
                    },
                )
            }
            AnimatedVisibility(
                visible = screenState.filterButtonVisible && screenState.homeFilters.isNotEmpty(),
            ) {
                IconButton(
                    onClick = onFilterClicked,
                    icon = {
                        BadgedBox(
                            badge = {
                                if (screenState.activeFilters > 0) {
                                    Badge(
                                        text = "${screenState.activeFilters}",
                                        color = onToolbarColor,
                                        textColor = primaryColor,
                                    )
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_filter),
                                contentDescription = "Filters",
                                tint = onToolbarColor,
                            )
                        }
                    },
                )
            }
        },
        title = {
            when (screenState.currentScreen) {
                MainScreenType.About -> R.string.about
                is MainScreenType.Home -> R.string.done_task
                MainScreenType.Loading -> null
                MainScreenType.QRScanner -> R.string.QR_SCANNER
                MainScreenType.Settings -> R.string.SYNC_MANAGER
                MainScreenType.TroubleShooting -> R.string.main_menu_troubleshooting
            }?.let { titleId ->
                Text(
                    text = stringResource(titleId),
                    color = onToolbarColor,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = primaryColor,
            titleContentColor = onToolbarColor,
        ),
    )
}

@Composable
fun HomeBottomBar(
    homeScreenState: HomeScreenState,
    onNavigationSelected: (NavigationPage) -> Unit,
) {
    val navigationItems = homeScreenState.navigationBarItems

    val selectedItemIndex by remember(homeScreenState) {
        derivedStateOf {
            when {
                homeScreenState.currentScreen.isPrograms() -> 0
                homeScreenState.currentScreen.isVisualizations() -> 1
                else -> null
            }
        }
    }
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        items = navigationItems,
        selectedItemIndex = selectedItemIndex ?: 0,
        onItemClick = { navigationPage -> onNavigationSelected(navigationPage) },
    )
}

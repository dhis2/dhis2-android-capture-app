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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.databinding.ActivityMainBinding
import org.dhis2.mobile.commons.extensions.ObserveAsEvents
import org.dhis2.mobile.home.ui.HomeNavigationReelItem
import org.dhis2.mobile.home.ui.component.ModalHomeNavigationRail
import org.dhis2.mobile.login.pin.domain.model.PinMode
import org.dhis2.mobile.login.pin.ui.components.ManagePinDialog
import org.dhis2.mobile.login.pin.ui.components.PinDialog
import org.dhis2.usescases.main.HomeScreen
import org.dhis2.usescases.main.MainScreenType
import org.dhis2.usescases.main.ui.model.HomeAction
import org.dhis2.usescases.main.ui.model.HomeEffect
import org.dhis2.usescases.main.ui.model.HomeScreenState
import org.dhis2.usescases.main.ui.model.PinAction
import org.dhis2.utils.customviews.navigationbar.NavigationPage
import org.dhis2.utils.getOnToolbarColor
import org.dhis2.utils.getThemePrimaryColor
import org.hisp.dhis.mobile.ui.designsystem.component.Badge
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.TopBar
import org.hisp.dhis.mobile.ui.designsystem.component.TopBarActionIcon
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow

@Composable
fun MainScreen(
    screenState: HomeScreenState,
    effects: Flow<HomeEffect>,
    onAction: (HomeAction) -> Unit,
    onEffect: (HomeEffect) -> Unit,
    onNewState: (HomeScreenState) -> Unit,
    onLayoutInflated: (ActivityMainBinding) -> Unit,
    displayDeveloperOptions: Boolean,
) {
    var pinAction by remember { mutableStateOf<PinAction>(PinAction.None) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pinCreatedMessage = stringResource(R.string.pin_created)
    val pinUpdatedMessage = stringResource(R.string.pin_updated)
    val pinDeletedMessage = stringResource(R.string.pin_deleted)
    BackHandler { onAction(HomeAction.BackPressed) }

    ObserveAsEvents(effects) { effect ->
        when (effect) {
            HomeEffect.ShowPinDialog -> {
                pinAction = PinAction.SetPin
            }

            HomeEffect.PinAlreadyCreated -> {
                pinAction = PinAction.AskPin
            }

            HomeEffect.PinCreated -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = pinCreatedMessage,
                        duration = SnackbarDuration.Long,
                    )
                }
            }

            HomeEffect.PinUpdated -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = pinUpdatedMessage,
                        duration = SnackbarDuration.Long,
                    )
                }
            }

            HomeEffect.PinDeleted -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = pinDeletedMessage,
                        duration = SnackbarDuration.Long,
                    )
                }
            }

            else -> {
                onEffect(effect)
            }
        }
    }

    LaunchedEffect(screenState) {
        onNewState(screenState)
    }

    val navigationRailState = rememberWideNavigationRailState()

    Scaffold(
        topBar = {
            HomeTopBar(
                screenState = screenState,
                onMenuClicked = { scope.launch { navigationRailState.expand() } },
                onSyncClicked = { onAction(HomeAction.SyncClicked) },
                onFilterClicked = { onAction(HomeAction.FilterClicked) },
            )
        },
        bottomBar = {
            if (screenState.bottomNavigationBarVisible) {
                HomeBottomBar(screenState) { navigationPage ->
                    when (navigationPage) {
                        NavigationPage.ANALYTICS -> {
                            onAction(
                                HomeAction.ScreenChanged(
                                    MainScreenType.Home(
                                        HomeScreen.Visualizations,
                                    ),
                                ),
                            )
                        }

                        NavigationPage.PROGRAMS -> {
                            onAction(
                                HomeAction.ScreenChanged(
                                    MainScreenType.Home(
                                        HomeScreen.Programs,
                                    ),
                                ),
                            )
                        }

                        else -> { // no op
                        }
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.dropShadow(shape = SnackbarDefaults.shape),
                    snackbarData = data,
                )
            }
        },
    ) { paddingValues ->
        AndroidView(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            factory = { context ->
                ActivityMainBinding
                    .inflate(LayoutInflater.from(context))
                    .also { onLayoutInflated(it) }
                    .root
            },
        )
        when (pinAction) {
            PinAction.AskPin -> {
                PinDialog(
                    mode = PinMode.ASK,
                    onSuccess = {
                        pinAction = PinAction.ManagePin
                    },
                    onDismiss = { pinAction = PinAction.None },
                    onForgotPin = {
                        pinAction = PinAction.None
                        onAction(HomeAction.ForgotPin)
                    },
                )
            }

            PinAction.ManagePin -> {
                ManagePinDialog(
                    onUpdateClick = {
                        pinAction = PinAction.SetPin
                    },
                    onRemoveClick = {
                        onAction(HomeAction.DeletePin)
                        pinAction = PinAction.None
                    },
                    onDismiss = { pinAction = PinAction.None },
                )
            }

            PinAction.SetPin -> {
                PinDialog(
                    mode = PinMode.SET,
                    onSuccess = {
                        onAction(HomeAction.PinSet)
                        pinAction = PinAction.None
                    },
                    onDismiss = { pinAction = PinAction.None },
                )
            }

            PinAction.None -> {
                // no-op
            }
        }
    }
    // The modal rail draws in its own dialog window, so it needs no Box to overlay the content.
    ModalHomeNavigationRail(
        serverName = screenState.serverName,
        userName = screenState.userName,
        displayDeveloperOptions = displayDeveloperOptions,
        pinAlreadyCreated = screenState.pinAlreadyCreated,
        selectedItem = screenState.currentScreen.toNavigationItem(),
        state = navigationRailState,
        onItemSelected = { item -> onAction(item.toHomeAction()) },
    )
}

/** The navigation menu item that leads to this screen, if any. */
private fun MainScreenType.toNavigationItem(): HomeNavigationReelItem? =
    when (this) {
        is MainScreenType.Home -> HomeNavigationReelItem.Home
        MainScreenType.QRScanner -> HomeNavigationReelItem.ImportSharedData
        MainScreenType.Settings -> HomeNavigationReelItem.Settings
        MainScreenType.TroubleShooting -> HomeNavigationReelItem.Troubleshooting
        MainScreenType.About -> HomeNavigationReelItem.About
        MainScreenType.Loading -> null
    }

private fun HomeNavigationReelItem.toHomeAction(): HomeAction =
    when (this) {
        HomeNavigationReelItem.Home -> HomeAction.GoToHome
        HomeNavigationReelItem.ImportSharedData -> HomeAction.ScreenChanged(MainScreenType.QRScanner)
        HomeNavigationReelItem.Settings -> HomeAction.ScreenChanged(MainScreenType.Settings)
        is HomeNavigationReelItem.PinManagement -> HomeAction.BlockSession
        HomeNavigationReelItem.Logout -> HomeAction.LogOut
        HomeNavigationReelItem.Troubleshooting -> HomeAction.ScreenChanged(MainScreenType.TroubleShooting)
        HomeNavigationReelItem.DeveloperOptions -> HomeAction.OpenDeveloperOptions
        HomeNavigationReelItem.About -> HomeAction.ScreenChanged(MainScreenType.About)
        HomeNavigationReelItem.RemoveAccount -> HomeAction.DeleteAccount
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
        colors =
            TopAppBarDefaults.topAppBarColors().copy(
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
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        items = navigationItems,
        selectedItemIndex = selectedItemIndex ?: 0,
        onItemClick = { navigationPage -> onNavigationSelected(navigationPage) },
    )
}

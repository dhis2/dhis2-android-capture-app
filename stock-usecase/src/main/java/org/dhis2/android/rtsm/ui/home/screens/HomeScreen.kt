package org.dhis2.android.rtsm.ui.home.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.home.model.EditionDialogResult
import org.dhis2.android.rtsm.ui.home.model.ScreenAction
import org.dhis2.android.rtsm.ui.home.screens.components.Backdrop
import org.dhis2.android.rtsm.ui.home.screens.components.CompletionDialog
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.mobile.commons.extensions.ObserveAsEvents
import org.hisp.dhis.mobile.ui.designsystem.component.ExtendedFAB
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    manageStockViewModel: ManageStockViewModel = viewModel(),
    proceedAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> },
    syncAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> },
    onOpenAnalytics: (containerId: Int) -> Unit,
    onOpenOrgUnitTree: (hasUnsavedData: Boolean) -> Unit,
    onOpenManageStockBottomSheet: () -> Unit,
    onOpenDiscardTransactionBottomSheet: (
            (result: EditionDialogResult) -> Unit
    ) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()

    val dataEntryUiState by manageStockViewModel.dataEntryUiState.collectAsState()
    val scope = rememberCoroutineScope()
    val homeScreenState by viewModel.settingsUiState.collectAsState()
    val homeItems =
        listOf(
            NavigationBarItem(
                id = BottomNavigation.DATA_ENTRY.id,
                icon = Icons.AutoMirrored.Outlined.List,
                label = stringResource(R.string.navigation_data_entry),
            ),
            NavigationBarItem(
                id = BottomNavigation.ANALYTICS.id,
                icon = Icons.Outlined.BarChart,
                label = stringResource(R.string.section_charts),
            ),
        )

    ObserveAsEvents(
        flow = viewModel.action,
    ) { action ->
        when (action) {
            is ScreenAction.OpenAnalytics -> onOpenAnalytics(action.containerId)
            ScreenAction.OpenOrgUnitTree -> onOpenOrgUnitTree(manageStockViewModel.dataEntryUiState.value.hasUnsavedData)
            ScreenAction.OpenManageStockBottomSheet -> onOpenManageStockBottomSheet()
            is ScreenAction.OnDiscardTransaction -> onOpenDiscardTransactionBottomSheet(action.onResult)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        scaffoldState = scaffoldState,
        floatingActionButton = {
            AnimatedVisibility(
                visible = dataEntryUiState.button.visible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                ExtendedFAB(
                    text = stringResource(dataEntryUiState.button.text),
                    icon = {
                        Icon(
                            painter = painterResource(id = dataEntryUiState.button.icon),
                            contentDescription = stringResource(dataEntryUiState.button.text),
                            tint = Color.White,
                        )
                    },
                    onClick = {
                        proceedAction(scope, scaffoldState)
                    },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = it) {
                CompletionDialog(dataEntryUiState = dataEntryUiState)
            }
        },
        bottomBar = {
            if (homeScreenState.hasAnalytics) {
                NavigationBar(
                    items = homeItems,
                    selectedItemIndex = homeScreenState.selectedScreen.id,
                ) { itemId ->
                    viewModel.switchScreen(itemId)
                }
            }
        },
    ) { paddingValues ->

        AnimatedContent(
            homeScreenState.selectedScreen.id,
            label = "HomeScreenContent",
        ) { targetIndex ->
            when (targetIndex) {
                BottomNavigation.ANALYTICS.id -> {
                    AnalyticsScreen(
                        viewModel = viewModel,
                        backAction = { manageStockViewModel.onHandleBackNavigation() },
                        modifier = Modifier.padding(paddingValues),
                        scaffoldState = scaffoldState,
                    )
                }

                BottomNavigation.DATA_ENTRY.id -> {
                    Backdrop(
                        viewModel = viewModel,
                        manageStockViewModel = manageStockViewModel,
                        modifier = Modifier.padding(paddingValues),
                        scaffoldState = scaffoldState,
                        syncAction = syncAction,
                    )
                }
            }
        }
    }
}

enum class BottomNavigation(
    val id: Int,
) {
    DATA_ENTRY(0),
    ANALYTICS(1),
}

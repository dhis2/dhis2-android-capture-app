package org.dhis2.android.rtsm.ui.home.screens

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
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
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.home.screens.components.Backdrop
import org.dhis2.android.rtsm.ui.home.screens.components.CompletionDialog
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.ExtendedFAB
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

@Composable
fun HomeScreen(
    activity: Activity,
    viewModel: HomeViewModel = viewModel(),
    manageStockViewModel: ManageStockViewModel = viewModel(),
    themeColor: Color,
    supportFragmentManager: FragmentManager,
    barcodeLauncher: ActivityResultLauncher<ScanOptions>,
    proceedAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> },
    syncAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> },
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
                label = activity.getString(R.string.navigation_data_entry),
            ),
            NavigationBarItem(
                id = BottomNavigation.ANALYTICS.id,
                icon = Icons.Outlined.BarChart,
                label = activity.getString(R.string.section_charts),
            ),
        )
    Scaffold(
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
                            tint = dataEntryUiState.button.contentColor,
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
                    DHIS2Theme {
                        AnalyticsScreen(
                            viewModel = viewModel,
                            backAction = { manageStockViewModel.onHandleBackNavigation() },
                            themeColor = themeColor,
                            modifier = Modifier.padding(paddingValues),
                            scaffoldState = scaffoldState,
                            supportFragmentManager = supportFragmentManager,
                        )
                    }
                }

                BottomNavigation.DATA_ENTRY.id -> {
                    Backdrop(
                        activity = activity,
                        viewModel = viewModel,
                        manageStockViewModel = manageStockViewModel,
                        modifier = Modifier.padding(paddingValues),
                        themeColor = themeColor,
                        supportFragmentManager = supportFragmentManager,
                        barcodeLauncher = barcodeLauncher,
                        scaffoldState = scaffoldState,
                    ) { coroutineScope, scaffold ->
                        syncAction(coroutineScope, scaffold)
                    }
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

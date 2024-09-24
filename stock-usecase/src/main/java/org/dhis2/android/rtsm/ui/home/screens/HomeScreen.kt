package org.dhis2.android.rtsm.ui.home.screens

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.home.screens.components.Backdrop
import org.dhis2.android.rtsm.ui.home.screens.components.CompletionDialog
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.ui.buttons.FAButton
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem

@OptIn(ExperimentalMaterialApi::class)
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
    var selectedIndex by remember { mutableIntStateOf(0) }
    val analytics by viewModel.analytics.collectAsState()

    val homeItems = listOf(
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
                FAButton(
                    text = dataEntryUiState.button.text,
                    contentColor = dataEntryUiState.button.contentColor,
                    containerColor = dataEntryUiState.button.containerColor,
                    icon = {
                        Icon(
                            painter = painterResource(id = dataEntryUiState.button.icon),
                            contentDescription = stringResource(dataEntryUiState.button.text),
                            tint = dataEntryUiState.button.contentColor,
                        )
                    },
                ) {
                    proceedAction(scope, scaffoldState)
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = it) {
                CompletionDialog(dataEntryUiState = dataEntryUiState)
            }
        },
        bottomBar = {
            if (analytics.isNotEmpty()) {
                NavigationBar(
                    items = homeItems,
                    selectedItemIndex = selectedIndex,
                ) { itemId ->

                    selectedIndex = homeItems.indexOfFirst { it.id == itemId }
                }
            }
        },
    ) { paddingValues ->

        AnimatedContent(
            selectedIndex,
            transitionSpec = {
                slideIn(
                    animationSpec = spring(3000F),
                    initialOffset = {
                        if (selectedIndex == BottomNavigation.ANALYTICS.id) {
                            IntOffset(300, 0)
                        } else {
                            IntOffset(-300, 0)
                        }
                    },
                ) togetherWith slideOut(
                    animationSpec = spring(3000F),
                    targetOffset = {
                        if (selectedIndex == BottomNavigation.ANALYTICS.id) {
                            IntOffset(-300, 0)
                        } else {
                            IntOffset(300, 0)
                        }
                    },
                )
            },
            label = "HomeScreenContent",
        ) { targetIndex ->
            when (targetIndex) {
                BottomNavigation.ANALYTICS.id ->
                    {
                        AnalyticsScreen(
                            viewModel = viewModel,
                            manageStockViewModel = manageStockViewModel,
                            themeColor = themeColor,
                            modifier = Modifier.padding(paddingValues),
                            scaffoldState = scaffoldState,
                            supportFragmentManager = supportFragmentManager,
                        )
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

        AnimatedVisibility(selectedIndex == BottomNavigation.ANALYTICS.id) {
        }

        AnimatedVisibility(selectedIndex != BottomNavigation.ANALYTICS.id) {
        }
    }
}

enum class BottomNavigation(val id: Int) {
    DATA_ENTRY(0),
    ANALYTICS(1),
}

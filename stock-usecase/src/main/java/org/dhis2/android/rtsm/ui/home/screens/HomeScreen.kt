package org.dhis2.android.rtsm.ui.home.screens

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import org.dhis2.ui.buttons.FAButton
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem

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

    val scope = rememberCoroutineScope()
    val dataEntryUiState by manageStockViewModel.dataEntryUiState.collectAsState()

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
    var selectedHomeItemIndex by remember { mutableIntStateOf(0) }
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
                    selectedItemIndex = selectedHomeItemIndex,
                ) { itemId ->

                    selectedHomeItemIndex = homeItems.indexOfFirst { it.id == itemId }
                }
            }
        },
    ) { paddingValues ->

        Backdrop(
            activity = activity,
            viewModel = viewModel,
            manageStockViewModel = manageStockViewModel,
            modifier = Modifier.padding(paddingValues),
            themeColor = themeColor,
            supportFragmentManager = supportFragmentManager,
            barcodeLauncher = barcodeLauncher,
            scaffoldState = scaffoldState,
            selectedHomeIndex = selectedHomeItemIndex,
        ) { coroutineScope, scaffold ->
            syncAction(coroutineScope, scaffold)
        }
    }
}

enum class BottomNavigation(val id: Int) {
    DATA_ENTRY(0),
    ANALYTICS(1),
}

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
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.home.screens.components.Backdrop
import org.dhis2.android.rtsm.ui.home.screens.components.CompletionDialog
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.ui.buttons.FAButton

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
        ) { coroutineScope, scaffold ->
            syncAction(coroutineScope, scaffold)
        }
    }
}

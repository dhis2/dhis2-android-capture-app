package org.dhis2.android.rtsm.ui.home.screens

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.home.model.ButtonVisibilityState.ENABLED
import org.dhis2.android.rtsm.ui.home.screens.components.Backdrop
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.ui.buttons.FAButton
import org.dhis2.ui.buttons.FAButtonUiModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    activity: Activity,
    viewModel: HomeViewModel = viewModel(),
    manageStockViewModel: ManageStockViewModel = viewModel(),
    themeColor: Color,
    supportFragmentManager: FragmentManager,
    barcodeLauncher: ActivityResultLauncher<ScanOptions>,
    proceedAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> },
    syncAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> }
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val dataEntryUiState by manageStockViewModel.dataEntryUiState.collectAsState()

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            AnimatedVisibility(
                visible = dataEntryUiState.button.visibility == ENABLED,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FAButton(
                    modifier = Modifier,
                    uiModel = FAButtonUiModel(
                        text = dataEntryUiState.button.text,
                        icon = dataEntryUiState.button.icon,
                        contentColor = themeColor,
                        containerColor = Color.White,
                        enabled = dataEntryUiState.button.visibility == ENABLED
                    )
                ) {
                    if (dataEntryUiState.button.visibility == ENABLED) {
                        proceedAction(scope, scaffoldState)
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = it) { data ->
                Snackbar(
                    snackbarData = data, backgroundColor = colorResource(R.color.error)
                )
            }
        }
    ) {
        it.calculateBottomPadding()
        Backdrop(
            activity,
            viewModel,
            manageStockViewModel,
            themeColor,
            supportFragmentManager,
            barcodeLauncher,
            scaffoldState
        ) { coroutineScope, scaffold ->
            syncAction(coroutineScope, scaffold)
        }
    }
}

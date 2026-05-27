package org.dhis2.android.rtsm.ui.home.screens

import android.view.View.generateViewId
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.home.LocalThemeColor
import org.dhis2.android.rtsm.ui.home.screens.components.AnalyticsTopBar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    backAction: () -> Unit,
    scaffoldState: ScaffoldState,
    syncAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> },
) {
    val backdropState = rememberBackdropScaffoldState(BackdropValue.Revealed)
    val settingsUiState by viewModel.settingsUiState.collectAsState()
    BackdropScaffold(
        modifier = modifier,
        appBar = {
            AnalyticsTopBar(
                title = settingsUiState.programName,
                themeColor = LocalThemeColor.current,
                backAction = {
                    backAction.invoke()
                },
                scaffoldState = scaffoldState,
                syncAction = syncAction,
            )
        },
        backLayerBackgroundColor = LocalThemeColor.current,
        backLayerContent = {
        },
        frontLayerElevation = 0.dp,
        frontLayerContent = {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    FrameLayout(context).apply {
                        id = generateViewId()
                    }
                },
                update = { view ->
                    viewModel.openAnalyticsScreen(view.id)
                },
            )
        },
        scaffoldState = backdropState,
        gesturesEnabled = false,
        frontLayerBackgroundColor = Color.White,
        frontLayerScrimColor = Color.Unspecified,
    )
}

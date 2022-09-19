package org.dhis2.android.rtsm.ui.home.screens.components

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import org.dhis2.android.rtsm.ui.home.HomeViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Backdrop(
    activity: Activity,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    themeColor: Color,
    scaffoldState: ScaffoldState,
    syncAction: (scope: CoroutineScope, scaffoldState: ScaffoldState) -> Unit = { _, _ -> }
) {
    val backdropState = rememberBackdropScaffoldState(BackdropValue.Concealed)

    BackdropScaffold(
        appBar = {
            Toolbar(
                viewModel.toolbarTitle.collectAsState().value.name,
                viewModel.fromFacility.collectAsState().value,
                viewModel.deliveryTo.collectAsState().value,
                themeColor,
                navigationAction = {
                    activity.finish()
                },
                backdropState,
                scaffoldState,
                syncAction
            )
        },
        backLayerBackgroundColor = themeColor,
        backLayerContent = {
            FilterList(viewModel, themeColor)
        },
        frontLayerElevation = 5.dp,
        frontLayerContent = {
            Column(Modifier.padding(16.dp)) {
                Text(text = "Main content")
            }
        },
        scaffoldState = backdropState,
        gesturesEnabled = false
    )
}

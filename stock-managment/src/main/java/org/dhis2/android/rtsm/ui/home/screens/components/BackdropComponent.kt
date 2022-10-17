package org.dhis2.android.rtsm.ui.home.screens.components

import android.app.Activity
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import org.dhis2.android.rtsm.data.TransactionType
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
    val backdropState = rememberBackdropScaffoldState(BackdropValue.Revealed)

    var hasFacilitySelected by remember { mutableStateOf(false) }
    var hasDestinationSelected by remember { mutableStateOf<Boolean?>(null) }
    var toolbarTitle by remember {
        mutableStateOf(TransactionType.DISTRIBUTION.name)
    }
    var dpHeight by remember { mutableStateOf<Dp?>(0.dp) }

    BackdropScaffold(
        appBar = {
            Toolbar(
                viewModel.toolbarTitle.collectAsState().value.name,
                viewModel.fromFacility.collectAsState().value.asString(),
                viewModel.deliveryTo.collectAsState().value?.asString(),
                themeColor,
                navigationAction = {
                    activity.finish()
                },
                backdropState,
                scaffoldState,
                syncAction,
                hasFacilitySelected,
                hasDestinationSelected
            )
            toolbarTitle = viewModel.toolbarTitle.collectAsState().value.name
        },
        backLayerBackgroundColor = themeColor,
        backLayerContent = {
            dpHeight = FilterList(
                viewModel, themeColor,
                { hasFacilitySelected = it },
                { hasDestinationSelected = it }
            )
        },
        frontLayerElevation = 5.dp,
        frontLayerContent = {
            MainContent(dpHeight!!, backdropState, toolbarTitle)
        },
        scaffoldState = backdropState,
        gesturesEnabled = false,
        frontLayerScrimColor = if (toolbarTitle == TransactionType.DISTRIBUTION.name) {
            if (hasFacilitySelected && hasDestinationSelected == true) {
                Color.Unspecified
            } else {
                MaterialTheme.colors.surface.copy(alpha = 0.60f)
            }
        } else {
            if (!hasFacilitySelected) {
                MaterialTheme.colors.surface.copy(alpha = 0.60f)
            } else {
                Color.Unspecified
            }
        }
    )
}

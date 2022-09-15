@file:Suppress("PreviewMustBeTopLevelFunction", "PreviewAnnotationInFunctionWithParameters")

package org.dhis2.android.rtsm.ui.home.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.home.screens.components.Toolbar


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun Backdrop(
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    themeColor: Color,
    navigationAction: () -> Unit,
) {
    val backdropState = rememberBackdropScaffoldState(BackdropValue.Concealed)

    BackdropScaffold(
        appBar = {
            Toolbar(
                viewModel.toolbarTitle.value.name,
                viewModel.toolbarSubtitle.value,
                themeColor, navigationAction, backdropState)
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
        gesturesEnabled = false,
    )
}
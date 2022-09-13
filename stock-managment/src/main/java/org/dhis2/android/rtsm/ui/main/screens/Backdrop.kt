@file:Suppress("PreviewMustBeTopLevelFunction", "PreviewAnnotationInFunctionWithParameters")

package org.dhis2.android.rtsm.ui.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.ui.home.HomeViewModel


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun Backdrop(
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navigationAction: () -> Unit,
) {

    val scope = rememberCoroutineScope()
    val selection = remember { mutableStateOf(0) }
    val backdropState = rememberBackdropScaffoldState(BackdropValue.Concealed)

    BackdropScaffold(
        appBar = {
             TopAppBar(
                 title = {
                     Column(
                         verticalArrangement = Arrangement.Bottom,
                         horizontalAlignment = Alignment.Start
                     ) {
                         Text(
                             text = viewModel.toolbarTitle.value.toString().ifEmpty { "Home" },
                             style = MaterialTheme.typography.subtitle1,
                             maxLines = 1,
                             fontSize = 20.sp
                         )
                         Text(
                             text = "From facility -> Delivery To",
                             style = MaterialTheme.typography.subtitle2,
                             softWrap = true,
                             overflow = TextOverflow.Ellipsis,
                             maxLines = 1,
                             fontSize = 12.sp
                         )
                     }
                 },
                 navigationIcon = {
                     IconButton(onClick = { navigationAction() }) {
                         Icon(
                             imageVector = Icons.Filled.ArrowBack,
                             contentDescription = stringResource(R.string.back)
                         )
                     }
                 },
                 actions = {
                     Row(
                         verticalAlignment = Alignment.CenterVertically,
                         horizontalArrangement = Arrangement.End
                     ) {
                         IconButton(
                             onClick = {}
                         ) {
                             Icon(
                                 painter = painterResource(R.drawable.ic_sync),
                                 contentDescription = null,
                                 tint = colorResource(id = R.color.white)
                             )
                         }

                         IconButton(
                             onClick = {
                                 if (backdropState.isConcealed) {
                                    scope.launch { backdropState.reveal() }
                                 } else scope.launch { backdropState.conceal() }
                             }
                         ) {
                             Icon(
                                 painter = painterResource(R.drawable.ic_filter),
                                 contentDescription = null,
                                 tint = colorResource(id = R.color.white)
                             )
                         }
                     }
                 },
                 backgroundColor = colorResource(R.attr.colorPrimary),
                 contentColor = Color.White,
                 elevation = 0.dp,
             )
        },
        backLayerBackgroundColor = colorResource(R.color.colorPrimary),
        backLayerContent = {
            val data = mutableListOf(
                mutableListOf("Org Unit 1", "Org Unit 2", "Org Unit 3"),
                mutableListOf("Org Unit A", "Org Unit B", "Org Unit C"),
                mutableListOf("Org Unit 1A", "Org Unit 2B", "Org Unit 3C"),
                mutableListOf("Org Unit 1", "Org Unit 2", "Org Unit 3"),
                mutableListOf("Org Unit A", "Org Unit B", "Org Unit C"),
                mutableListOf("Org Unit 1A", "Org Unit 2B", "Org Unit 3C"),
            )

            FilterList(data)
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
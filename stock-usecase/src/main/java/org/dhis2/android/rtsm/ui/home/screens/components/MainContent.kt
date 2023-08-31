package org.dhis2.android.rtsm.ui.home.screens.components

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.home.model.DataEntryStep
import org.dhis2.android.rtsm.ui.home.model.SettingsUiState
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.android.rtsm.ui.managestock.STOCK_TABLE_ID
import org.dhis2.android.rtsm.ui.managestock.components.ManageStockTable
import org.dhis2.android.rtsm.ui.scanner.ScannerActivity
import org.dhis2.composetable.actions.TableResizeActions

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainContent(
    backdropState: BackdropScaffoldState,
    isFrontLayerDisabled: Boolean?,
    themeColor: Color,
    viewModel: HomeViewModel,
    manageStockViewModel: ManageStockViewModel,
    barcodeLauncher: ActivityResultLauncher<ScanOptions>
) {
    val scope = rememberCoroutineScope()
    val resource = painterResource(R.drawable.ic_arrow_up)
    val qrcodeResource = painterResource(R.drawable.ic_qr_code_scanner)
    val resetResize = painterResource(id = R.drawable.ic_restart_alt)
    val searchResource = painterResource(R.drawable.ic_search)
    val closeResource = painterResource(R.drawable.ic_close)
    var closeButtonVisibility by remember { mutableStateOf(0f) }
    val weightValue = if (backdropState.isRevealed) 0.15f else 0.10f
    val weightValueArrow = if (backdropState.isRevealed) 0.10f else 0.05f
    val weightValueArrowStatus = backdropState.isRevealed
    val focusManager = LocalFocusManager.current
    val search by manageStockViewModel.scanText.collectAsState()
    val settingsUiState by viewModel.settingsUiState.collectAsState()
    var columnHeightDp by remember { mutableStateOf(0.dp) }
    val localDensity = LocalDensity.current
    val tablePadding = if (backdropState.isRevealed) 200.dp else 0.dp

    var tableResizeActions by remember {
        mutableStateOf<TableResizeActions?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                columnHeightDp = with(localDensity) { coordinates.size.height.toDp() }
            }
            .onSizeChanged { coordinates ->
                columnHeightDp = with(localDensity) { coordinates.height.toDp() }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .absolutePadding(
                    left = 16.dp,
                    top = 16.dp,
                    right = 16.dp
                )
                .fillMaxWidth()
                .size(60.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = manageStockViewModel::onSearchQueryChanged,
                modifier = Modifier
                    .background(Color.White, shape = CircleShape)
                    .padding(end = 16.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(30.dp),
                        clip = false
                    )
                    .offset(0.dp, 0.dp)
                    .background(color = Color.White, shape = RoundedCornerShape(30.dp))
                    .weight(1 - (weightValue + weightValueArrow))
                    .alignBy(FirstBaseline)
                    .align(alignment = Alignment.CenterVertically)
                    .onFocusChanged {
                        if (it.hasFocus) {
                            scope.launch {
                                backdropState.conceal()
                            }
                        }
                    },
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    cursorColor = themeColor
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                enabled = isFrontLayerDisabled != true,
                leadingIcon = {
                    Icon(
                        painter = searchResource,
                        contentDescription = "",
                        tint = themeColor
                    )
                },
                trailingIcon = {
                    IconButton(
                        modifier = Modifier
                            .alpha(closeButtonVisibility),
                        onClick = {
                            manageStockViewModel.onSearchQueryChanged("")
                            closeButtonVisibility = 0f
                        }
                    ) {
                        Icon(
                            painter = closeResource,
                            contentDescription = ""
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        manageStockViewModel.onSearchQueryChanged(search)
                    },
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                singleLine = true,
                placeholder = {
                    Text(text = stringResource(id = R.string.search_placeholder))
                }
            )
            IconButton(
                onClick = {
                    scanBarcode(barcodeLauncher)
                },
                modifier = Modifier
                    .weight(weightValue)
                    .alignBy(FirstBaseline)
                    .align(alignment = Alignment.CenterVertically),
                enabled = isFrontLayerDisabled != true
            ) {
                Icon(
                    painter = qrcodeResource,
                    contentDescription = "",
                    tint = themeColor
                )
            }

            AnimatedVisibility(visible = tableResizeActions != null) {
                IconButton(
                    onClick = {
                        tableResizeActions?.onTableDimensionReset(STOCK_TABLE_ID)
                    },
                    modifier = Modifier
                        .weight(weightValue)
                        .alignBy(FirstBaseline)
                        .align(alignment = Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = resetResize,
                        contentDescription = "",
                        tint = themeColor
                    )
                }
            }

            AnimatedVisibility(
                visible = backdropState.isRevealed
            ) {
                IconButton(
                    onClick = {
                        scope.launch { backdropState.conceal() }
                    },
                    modifier = Modifier
                        .weight(weightValueArrow, weightValueArrowStatus)
                        .alignBy(FirstBaseline)
                        .align(alignment = Alignment.CenterVertically)
                ) {
                    Icon(
                        resource,
                        contentDescription = null,
                        tint = themeColor
                    )
                }
            }
            closeButtonVisibility = when (search) {
                "" -> 0f
                else -> 1f
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(bottom = tablePadding)
                .height(columnHeightDp)
        ) {
            if ((
                manageStockViewModel.dataEntryUiState.collectAsState().value.step
                    != DataEntryStep.COMPLETED ||
                    manageStockViewModel.dataEntryUiState.collectAsState().value.step
                    != DataEntryStep.START
                ) && shouldDisplayTable(settingsUiState)
            ) {
                manageStockViewModel.setup(viewModel.getData())
                ManageStockTable(
                    manageStockViewModel,
                    concealBackdropState = {
                        scope.launch { backdropState.conceal() }
                    },
                    onResized = { actions ->
                        manageStockViewModel.refreshConfig()
                        tableResizeActions = actions
                    }
                )
            }
        }
    }
}

private fun shouldDisplayTable(settingsUiState: SettingsUiState): Boolean =
    when (settingsUiState.transactionType) {
        TransactionType.DISTRIBUTION ->
            settingsUiState.hasFacilitySelected() && settingsUiState.hasDestinationSelected()
        else -> settingsUiState.hasFacilitySelected()
    }

private fun scanBarcode(launcher: ActivityResultLauncher<ScanOptions>) {
    val scanOptions = ScanOptions()
        .setBeepEnabled(true)
        .setCaptureActivity(ScannerActivity::class.java)
    launcher.launch(scanOptions)
}

package org.dhis2.usescases.searchTrackEntity.searchparameters

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.intents.CustomIntentAction
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.data.scan.ScanContract
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.searchparameters.model.SearchParametersUiState
import org.dhis2.usescases.searchTrackEntity.searchparameters.provider.provideParameterSelectorItem
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBarData
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.ParameterSelectorItem
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun SearchParametersScreen(
    resourceManager: ResourceManager,
    uiState: SearchParametersUiState,
    intentHandler: (FormIntent) -> Unit,
    onShowOrgUnit: (
        uid: String,
        preselectedOrgUnits: List<String>,
        orgUnitScope: OrgUnitSelectorScope,
        label: String,
    ) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val snackBarHostState = scaffoldState.snackbarHostState
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val configuration = LocalConfiguration.current

    val scanContract = remember { ScanContract() }
    val qrScanLauncher = rememberLauncherForActivityResult(
        contract = scanContract,
    ) { result ->
        result.contents?.let { qrData ->
            val intent = FormIntent.OnQrCodeScanned(
                uid = result.originalIntent.getStringExtra(Constants.UID)!!,
                value = qrData,
                valueType = ValueType.TEXT,
            )
            intentHandler(intent)
        }
    }

    val callback = remember {
        object : FieldUiModel.Callback {
            override fun intent(intent: FormIntent) {
                intentHandler.invoke(intent)
            }

            override fun recyclerViewUiEvents(uiEvent: RecyclerViewUiEvents) {
                when (uiEvent) {
                    is RecyclerViewUiEvents.OpenOrgUnitDialog ->
                        onShowOrgUnit(
                            uiEvent.uid,
                            uiEvent.value?.let { listOf(it) } ?: emptyList(),
                            uiEvent.orgUnitSelectorScope ?: OrgUnitSelectorScope.UserSearchScope(),
                            uiEvent.label,
                        )

                    is RecyclerViewUiEvents.ScanQRCode -> {
                        qrScanLauncher.launch(
                            ScanOptions().apply {
                                setDesiredBarcodeFormats()
                                setPrompt("")
                                setBeepEnabled(true)
                                setBarcodeImageEnabled(false)
                                addExtra(Constants.UID, uiEvent.uid)
                                uiEvent.optionSet?.let {
                                    addExtra(
                                        Constants.OPTION_SET,
                                        uiEvent.optionSet,
                                    )
                                }
                                addExtra(Constants.SCAN_RENDERING_TYPE, uiEvent.renderingType)
                            },
                        )
                    }

                    else -> {
                        // no-op
                    }
                }
            }
        }
    }

    uiState.minAttributesMessage?.let { message ->
        coroutineScope.launch {
            uiState.shouldShowMinAttributeWarning.collectLatest {
                if (it) {
                    snackBarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short,
                    )
                    uiState.updateMinAttributeWarning(false)
                }
            }
        }
    }

    LaunchedEffect(uiState.isOnBackPressed) {
        uiState.isOnBackPressed.collectLatest {
            if (it) {
                focusManager.clearFocus()
                onClose()
            }
        }
    }

    val backgroundShape = when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> RoundedCornerShape(
            topStart = CornerSize(Radius.L),
            topEnd = CornerSize(Radius.NoRounding),
            bottomEnd = CornerSize(Radius.NoRounding),
            bottomStart = CornerSize(Radius.NoRounding),
        )

        else -> Shape.LargeTop
    }

    Scaffold(
        backgroundColor = Color.Transparent,
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.padding(
                    start = 8.dp,
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 48.dp,
                ),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White, shape = backgroundShape)
                .padding(it),
        ) {
            Column(
                modifier = Modifier
                    .weight(1F)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (uiState.items.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        InfoBar(
                            infoBarData = InfoBarData(
                                text = resourceManager.getString(R.string.empty_search_attributes_message),
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = "warning",
                                        tint = AdditionalInfoItemColor.WARNING.color,
                                    )
                                },
                                color = AdditionalInfoItemColor.WARNING.color,
                                backgroundColor = AdditionalInfoItemColor.WARNING.color.copy(alpha = 0.1f),
                                actionText = null,
                                onClick = {},
                            ),
                            Modifier.testTag("EMPTY_SEARCH_ATTRIBUTES_TEXT_TAG"),
                        )
                    }
                } else {
                    uiState.items.forEachIndexed { index, fieldUiModel ->
                        fieldUiModel.setCallback(callback)
                        ParameterSelectorItem(
                            modifier = Modifier
                                .testTag("SEARCH_PARAM_ITEM"),
                            model = provideParameterSelectorItem(
                                resources = resourceManager,
                                focusManager = focusManager,
                                fieldUiModel = fieldUiModel,
                                callback = callback,
                                onNextClicked = {
                                    val nextIndex = index + 1
                                    if (nextIndex < uiState.items.size) {
                                        uiState.items[nextIndex].onItemClick()
                                    }
                                },
                            ),
                        )
                    }
                }

                if (uiState.clearSearchEnabled) {
                    Button(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp, 24.dp, 16.dp, 8.dp),
                        style = ButtonStyle.TEXT,
                        text = resourceManager.getString(R.string.clear_search),
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Cancel,
                                contentDescription = resourceManager.getString(R.string.clear_search),
                                tint = SurfaceColor.Primary,
                            )
                        },
                    ) {
                        focusManager.clearFocus()
                        onClear()
                    }
                }
            }

            Button(
                enabled = uiState.searchEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 16.dp, 8.dp)
                    .testTag("SEARCH_BUTTON"),
                style = ButtonStyle.FILLED,
                text = resourceManager.getString(R.string.search),
                icon = {
                    val iconTint = if (uiState.searchEnabled) {
                        TextColor.OnPrimary
                    } else {
                        TextColor.OnDisabledSurface
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = null,
                        tint = iconTint,
                    )
                },
            ) {
                focusManager.clearFocus()
                onSearch()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchFormPreview() {
    SearchParametersScreen(
        resourceManager = ResourceManager(LocalContext.current, ColorUtils()),
        uiState = SearchParametersUiState(
            items = listOf(
                FieldUiModelImpl(
                    uid = "uid1",
                    label = "Label 1",
                    autocompleteList = emptyList(),
                    optionSetConfiguration = null,
                    valueType = ValueType.TEXT,
                ),
                FieldUiModelImpl(
                    uid = "uid2",
                    label = "Label 2",
                    autocompleteList = emptyList(),
                    optionSetConfiguration = null,
                    valueType = ValueType.TEXT,
                ),
            ),
        ),
        intentHandler = {},
        onShowOrgUnit = { _, _, _, _ -> },
        onSearch = {},
        onClear = {},
        onClose = {},
    )
}

fun initSearchScreen(
    composeView: ComposeView,
    viewModel: SearchTEIViewModel,
    program: String?,
    teiType: String,
    resources: ResourceManager,
    onShowOrgUnit: (
        uid: String,
        preselectedOrgUnits: List<String>,
        orgUnitScope: OrgUnitSelectorScope,
        label: String,
    ) -> Unit,
    onClear: () -> Unit,
) {
    viewModel.fetchSearchParameters(
        programUid = program,
        teiTypeUid = teiType,
    )
    composeView.setContent {
        SearchParametersScreen(
            resourceManager = resources,
            uiState = viewModel.uiState,
            onSearch = viewModel::onSearch,
            intentHandler = viewModel::onParameterIntent,
            onShowOrgUnit = onShowOrgUnit,
            onClear = {
                onClear()
                viewModel.clearQueryData()
                viewModel.clearFocus()
            },
            onClose = { viewModel.clearFocus() },
        )
    }
}

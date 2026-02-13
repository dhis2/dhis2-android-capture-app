package org.dhis2.usescases.searchTrackEntity.searchparameters

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.data.scan.ScanContract
import org.dhis2.form.ui.customintent.CustomIntentActivityResultContract
import org.dhis2.form.ui.customintent.CustomIntentInput
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.mobile.commons.extensions.ObserveAsEvents
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.dhis2.tracker.search.ui.provider.provideParameterSelectorItem
import org.dhis2.tracker.ui.input.action.CustomIntentUid
import org.dhis2.tracker.ui.input.action.FieldUid
import org.dhis2.tracker.ui.input.action.TrackerInputAction
import org.dhis2.tracker.ui.input.model.TrackerInputModel
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.dhis2.tracker.ui.input.model.TrackerInputUiEvent
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.searchparameters.model.SearchParametersUiState
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation
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
    onLaunchCustomIntent: (FieldUid, CustomIntentUid) -> Unit,
    onItemClick: (String) -> Unit,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val configuration = LocalConfiguration.current

    val scanContract = remember { ScanContract() }
    val qrScanLauncher =
        rememberLauncherForActivityResult(
            contract = scanContract,
        ) { result ->
            result.contents?.let { qrData ->
                val intent =
                    FormIntent.OnQrCodeScanned(
                        uid = result.originalIntent.getStringExtra(Constants.UID)!!,
                        value = qrData,
                        valueType = ValueType.TEXT,
                    )
                intentHandler(intent)
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

    val backgroundShape =
        when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE ->
                RoundedCornerShape(
                    topStart = CornerSize(Radius.L),
                    topEnd = CornerSize(Radius.NoRounding),
                    bottomEnd = CornerSize(Radius.NoRounding),
                    bottomStart = CornerSize(Radius.NoRounding),
                )

            else -> Shape.LargeTop
        }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier =
                    Modifier.padding(
                        start = 8.dp,
                        top = 8.dp,
                        end = 8.dp,
                        bottom = 48.dp,
                    ),
            )
        },
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(color = Color.White, shape = backgroundShape)
                    .padding(
                        top = 0.dp,
                        bottom = paddingValues.calculateBottomPadding(),
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        end = paddingValues.calculateEndPadding(layoutDirection),
                    ),
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .weight(1F),
            ) {
                if (uiState.items.isEmpty()) {
                    item {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            InfoBar(
                                modifier = Modifier.testTag("EMPTY_SEARCH_ATTRIBUTES_TEXT_TAG"),
                                text = resourceManager.getString(R.string.empty_search_attributes_message),
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = "warning",
                                        tint = AdditionalInfoItemColor.WARNING.color,
                                    )
                                },
                                textColor = AdditionalInfoItemColor.WARNING.color,
                                backgroundColor = AdditionalInfoItemColor.WARNING.color.copy(alpha = 0.1f),
                            )
                        }
                    }
                } else {
                    itemsIndexed(
                        items = uiState.items,
                        key = { _, fieldUiModel ->
                            fieldUiModel.uid
                        },
                    ) { index, trackerInputModel ->
                        ParameterSelectorItem(
                            modifier =
                                Modifier
                                    .testTag("SEARCH_PARAM_ITEM"),
                            model =
                                provideParameterSelectorItem(
                                    inputModel =
                                    trackerInputModel,
                                    // TODO is this always the same string?, check if it is optional somewhere
                                    helperText = resourceManager.getString(R.string.optional),
                                    onNextClicked = {
                                        val nextIndex = index + 1
                                        if (nextIndex < uiState.items.size) {
                                            // TODO implement on next click
                                            // uiState.items[nextIndex].onItemClick()
                                        }
                                    },
                                    onUiEvent = { uiEvent ->
                                        when (uiEvent) {
                                            is TrackerInputUiEvent.OnScanButtonClicked -> {
//                                                // TODO Implement launcher from the outside
//                                                qrScanLauncher.launch(
//                                                    ScanOptions().apply {
//                                                        setDesiredBarcodeFormats()
//                                                        setPrompt("")
//                                                        setBeepEnabled(true)
//                                                        setBarcodeImageEnabled(false)
//                                                        addExtra(Constants.UID, uiEvent.uid)
//                                                        fieldUiModel.optionSet?.let {
//                                                            addExtra(
//                                                                Constants.OPTION_SET,
//                                                                it,
//                                                            )
//                                                        }
//                                                        addExtra(
//                                                            Constants.SCAN_RENDERING_TYPE,
//                                                            fieldUiModel.renderingType,
//                                                        )
//                                                    },
//                                                )
                                            }

                                            is TrackerInputUiEvent.OnOrgUnitButtonClicked -> {
                                                // TODO recover the orgUnitSelectorScope from the new model
                                                onShowOrgUnit(
                                                    uiEvent.uid,
                                                    uiEvent.value?.let { listOf(it) }
                                                        ?: emptyList(),
                                                    trackerInputModel.orgUnitSelectorScope
                                                        ?: OrgUnitSelectorScope.UserSearchScope(),
                                                    uiEvent.label,
                                                )
                                            }

                                            is TrackerInputUiEvent.OnLaunchCustomIntent -> {
                                                onLaunchCustomIntent(
                                                    uiEvent.uid,
                                                    uiEvent.customIntentUid,
                                                )
                                            }

                                            is TrackerInputUiEvent.OnItemClick -> {
                                                onItemClick(uiEvent.uid)
                                            }

                                            is TrackerInputUiEvent.OnValueChange -> {
                                                // TODO handle on save value differently
                                                intentHandler.invoke(
                                                    FormIntent.OnSave(
                                                        uid = uiEvent.uid,
                                                        value = uiEvent.value,
                                                        valueType = ValueType.TEXT,
                                                    ),
                                                )
                                            }
                                        }
                                    },
                                ),
                        )
                    }
                }

                if (uiState.clearSearchEnabled) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Button(
                                modifier = Modifier.padding(16.dp, 24.dp, 16.dp, 8.dp),
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
                }
            }

            Button(
                enabled = uiState.searchEnabled,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 8.dp, 16.dp, 8.dp)
                        .testTag("SEARCH_BUTTON"),
                style = ButtonStyle.FILLED,
                text = resourceManager.getString(R.string.search),
                icon = {
                    val iconTint =
                        if (uiState.searchEnabled) {
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
        uiState =
            SearchParametersUiState(
                items =
                    buildList {
                        repeat(times = 20) { index ->
                            add(
                                TrackerInputModel(
                                    uid = "uid$index",
                                    label = "Label $index",
                                    value = "test value",
                                    valueType = TrackerInputType.TEXT,
                                    focused = false,
                                    optionSet = null,
                                    error = null,
                                    warning = null,
                                    description = null,
                                    mandatory = false,
                                    editable = true,
                                    legend = null,
                                    orientation = Orientation.HORIZONTAL,
                                    optionSetConfiguration = null,
                                    customIntentUid = null,
                                    displayName = "display name",
                                    orgUnitSelectorScope = null,
                                ),
                            )
                        }
                    },
            ),
        intentHandler = {},
        onShowOrgUnit = { _, _, _, _ -> },
        onSearch = {},
        onClear = {},
        onClose = {},
        onLaunchCustomIntent = { _, _ -> },
        onItemClick = { _ -> },
    )
}

@Preview(showBackground = true)
@Composable
fun SearchFormPreviewWithClear() {
    SearchParametersScreen(
        resourceManager = ResourceManager(LocalContext.current, ColorUtils()),
        uiState =
            SearchParametersUiState(
                items =
                    buildList {
                        repeat(times = 20) { index ->
                            add(
                                TrackerInputModel(
                                    uid = "uid$index",
                                    label = "Label $index",
                                    value = "test value",
                                    valueType = TrackerInputType.TEXT,
                                    focused = false,
                                    optionSet = null,
                                    error = null,
                                    warning = null,
                                    description = null,
                                    mandatory = false,
                                    editable = true,
                                    legend = null,
                                    orientation = Orientation.HORIZONTAL,
                                    optionSetConfiguration = null,
                                    customIntentUid = null,
                                    displayName = "display name",
                                    orgUnitSelectorScope = null,
                                ),
                            )
                        }
                    },
            ),
        intentHandler = {},
        onShowOrgUnit = { _, _, _, _ -> },
        onSearch = {},
        onClear = {},
        onClose = {},
        onLaunchCustomIntent = { _, _ -> },
        onItemClick = { _ -> },
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
        val launcher =
            rememberLauncherForActivityResult(
                contract = CustomIntentActivityResultContract(),
                onResult = viewModel::handleCustomIntentResult,
            )

        ObserveAsEvents(
            flow = viewModel.searchActions,
        ) { action ->
            when (action) {
                is TrackerInputAction.LaunchCustomIntent -> {
                    launcher.launch(
                        with(action) {
                            CustomIntentInput(
                                fieldUid = fieldUid,
                                customIntent = customIntentModel,
                                defaultTitle =
                                    customIntentModel.name
                                        ?: resources.getString(org.dhis2.form.R.string.select_app_intent),
                            )
                        },
                    )
                }
            }
        }

        SearchParametersScreen(
            resourceManager = resources,
            uiState = viewModel.searchParametersUiState,
            onSearch = viewModel::onSearch,
            intentHandler = viewModel::onParameterIntent,
            onShowOrgUnit = onShowOrgUnit,
            onClear = {
                onClear()
                viewModel.clearQueryData()
                viewModel.clearFocus()
            },
            onClose = { viewModel.clearFocus() },
            onLaunchCustomIntent = viewModel::onLaunchCustomIntent,
            onItemClick = viewModel::onItemClick,
        )
    }
}

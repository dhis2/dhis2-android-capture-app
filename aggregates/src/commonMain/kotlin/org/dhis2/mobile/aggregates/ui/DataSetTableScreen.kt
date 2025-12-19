@file:OptIn(ExperimentalMaterial3Api::class)

package org.dhis2.mobile.aggregates.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetEdition
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.dhis2.mobile.aggregates.model.NonEditableReason
import org.dhis2.mobile.aggregates.resources.Res
import org.dhis2.mobile.aggregates.resources.attribute_option_combo_no_access
import org.dhis2.mobile.aggregates.resources.attribute_option_combo_not_assigned_to_org_unit
import org.dhis2.mobile.aggregates.resources.dataset_closed
import org.dhis2.mobile.aggregates.resources.dataset_expired
import org.dhis2.mobile.aggregates.resources.empty_dataset_message
import org.dhis2.mobile.aggregates.resources.empty_section_message
import org.dhis2.mobile.aggregates.resources.no_data_write_access
import org.dhis2.mobile.aggregates.resources.org_unit_not_in_capture_scope
import org.dhis2.mobile.aggregates.resources.period_not_in_attribute_option_combo_range
import org.dhis2.mobile.aggregates.resources.period_not_in_org_unit_range
import org.dhis2.mobile.aggregates.ui.component.HtmlContentBox
import org.dhis2.mobile.aggregates.ui.component.ValidationBar
import org.dhis2.mobile.aggregates.ui.component.ValidationBottomSheet
import org.dhis2.mobile.aggregates.ui.constants.INPUT_DIALOG_TAG
import org.dhis2.mobile.aggregates.ui.constants.SAVE_BUTTON_TAG
import org.dhis2.mobile.aggregates.ui.constants.SYNC_BUTTON_TAG
import org.dhis2.mobile.aggregates.ui.inputs.InputProvider
import org.dhis2.mobile.aggregates.ui.inputs.ResizeAction
import org.dhis2.mobile.aggregates.ui.snackbar.DataSetSnackbarHost
import org.dhis2.mobile.aggregates.ui.snackbar.SnackbarController
import org.dhis2.mobile.aggregates.ui.states.CellSelectionState
import org.dhis2.mobile.aggregates.ui.states.DataSetScreenState
import org.dhis2.mobile.aggregates.ui.states.DataSetSectionTable
import org.dhis2.mobile.aggregates.ui.viewModel.DataSetTableViewModel
import org.dhis2.mobile.commons.extensions.ObserveAsEvents
import org.dhis2.mobile.commons.input.UiAction
import org.dhis2.mobile.commons.input.UiActionHandler
import org.dhis2.mobile.commons.ui.NonEditableReasonBlock
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.FAB
import org.hisp.dhis.mobile.ui.designsystem.component.FABStyle
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.InputDialog
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.component.TopBar
import org.hisp.dhis.mobile.ui.designsystem.component.TopBarActionIcon
import org.hisp.dhis.mobile.ui.designsystem.component.VerticalTabs
import org.hisp.dhis.mobile.ui.designsystem.component.layout.TwoPaneConfig
import org.hisp.dhis.mobile.ui.designsystem.component.layout.TwoPaneLayout
import org.hisp.dhis.mobile.ui.designsystem.component.model.Tab
import org.hisp.dhis.mobile.ui.designsystem.component.table.actions.TableInteractions
import org.hisp.dhis.mobile.ui.designsystem.component.table.actions.TableResizeActions
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.DataTable
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.TableDimensions
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.TableSelection
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.TableTheme
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val FabContainerHeight = 56.dp + 16.dp + 16.dp

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Data set table screen
 * Shows the data set details and tables
 * @param parameters: Data set instance parameters
 * @param useTwoPane: Whether to use a two pane layout
 * @param onBackClicked: Callback function to be invoked when the back button is clicked
 * @param onSyncClicked: Callback function to be invoked when the sync button is clicked
 * */
@Composable
fun DataSetInstanceScreen(
    parameters: DataSetInstanceParameters,
    useTwoPane: Boolean,
    snackbarHostState: SnackbarHostState,
    onBackClicked: () -> Unit,
    onSyncClicked: (onUpdateData: () -> Unit) -> Unit,
    uiActionHandler: UiActionHandler,
) {
    val dataSetTableViewModel: DataSetTableViewModel =
        koinViewModel<DataSetTableViewModel>(parameters = {
            parametersOf(
                parameters.dataSetUid,
                parameters.periodId,
                parameters.organisationUnitUid,
                parameters.attributeOptionComboUid,
                parameters.openErrorLocation,
                onBackClicked,
                uiActionHandler,
            )
        })
    val dataSetScreenState by dataSetTableViewModel.dataSetScreenState.collectAsState()

    val allowTwoPane by remember(useTwoPane, dataSetScreenState) {
        derivedStateOf {
            dataSetScreenState.allowTwoPane(useTwoPane)
        }
    }

    val scope = rememberCoroutineScope()

    ObserveAsEvents(
        flow = SnackbarController.events,
        snackbarHostState,
    ) { event ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()

            snackbarHostState.showSnackbar(
                message = event.message,
                duration = SnackbarDuration.Short,
            )
        }
    }

    val tableCellSelection =
        (dataSetScreenState as? DataSetScreenState.Loaded)?.selectedCellInfo?.tableSelection
            ?: TableSelection.Unselected()

    var fabSizeDp by remember { mutableStateOf(FabContainerHeight) }

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .imePadding(),
        topBar = {
            TopBar(
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    TopBarActionIcon(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = "back arrow",
                        onClick = onBackClicked,
                    )
                },
                actions = {
                    IconButton(
                        modifier = Modifier.testTag(SYNC_BUTTON_TAG),
                        style = IconButtonStyle.STANDARD,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = "Sync",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        },
                        onClick = { onSyncClicked({ dataSetTableViewModel.loadDataSet() }) },
                    )
                },
                title = {
                    if (dataSetScreenState is DataSetScreenState.Loaded) {
                        Text(
                            text = (dataSetScreenState as DataSetScreenState.Loaded).dataSetDetails.dataSetTitle,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors().copy(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        },
        snackbarHost = { DataSetSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            val loadedState = dataSetScreenState as? DataSetScreenState.Loaded
            val visible =
                loadedState?.dataSetSectionTable?.loading == false &&
                    loadedState.selectedCellInfo !is CellSelectionState.InputDataUiState
            fabSizeDp =
                if (visible) {
                    FabContainerHeight
                } else {
                    0.dp
                }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                FAB(
                    modifier = Modifier.testTag(SAVE_BUTTON_TAG),
                    style = FABStyle.SECONDARY,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Done,
                            contentDescription = "Save Button",
                        )
                    },
                    onClick = {
                        dataSetTableViewModel.onSaveClicked()
                    },
                )
            }
        },
        bottomBar = {
            (dataSetScreenState as? DataSetScreenState.Loaded)?.let { state ->
                when {
                    state.modalDialog != null ->
                        ValidationBottomSheet(dataSetUIState = (dataSetScreenState as? DataSetScreenState.Loaded)?.modalDialog!!)

                    state.validationBar != null ->
                        ValidationBar(uiState = (dataSetScreenState as? DataSetScreenState.Loaded)?.validationBar!!)

                    state.dataSetDetails.isCompleted or !state.dataSetDetails.edition.editable -> {
                        val edition = state.dataSetDetails.edition
                        NonEditableReasonBlock(
                            modifier = Modifier.fillMaxWidth(),
                            paddingValues =
                                PaddingValues(
                                    start = Spacing.Spacing16,
                                    top = Spacing.Spacing8,
                                    end = Spacing.Spacing16,
                                    bottom =
                                        Spacing.Spacing8 +
                                            WindowInsets.navigationBars
                                                .asPaddingValues()
                                                .calculateBottomPadding(),
                                ),
                            reason = nonEditableReasonLabel(edition),
                            canBeReopened = state.dataSetDetails.isCompleted && edition.editable,
                            onReopenClick = dataSetTableViewModel::onReopenDataSet,
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { paddingValues ->

        var inputDialogSize: Int? by remember { mutableStateOf(null) }
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            propagateMinConstraints = true,
        ) {
            if (allowTwoPane) {
                TwoPaneLayout(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(topStart = Radius.L, topEnd = Radius.L),
                            ),
                    paneConfig = TwoPaneConfig.SecondaryPaneFixedSize(283.dp),
                    primaryPane = {
                        when (dataSetScreenState) {
                            is DataSetScreenState.Loaded ->
                                DataSetTableContent(
                                    modifier =
                                        Modifier.background(
                                            color = MaterialTheme.colorScheme.background,
                                            shape = RoundedCornerShape(topEnd = Radius.L),
                                        ),
                                    dataSetDetails = (dataSetScreenState as DataSetScreenState.Loaded).dataSetDetails,
                                    dataSetSectionTable = (dataSetScreenState as DataSetScreenState.Loaded).dataSetSectionTable,
                                    inputDialogSize = inputDialogSize,
                                    fabSize = fabSizeDp,
                                    onCellClick = { cellId, cellValue, cellError ->
                                        scope.launch {
                                            dataSetTableViewModel.updateSelectedCell(
                                                cellId = cellId,
                                                newValue = cellValue,
                                                validationError = cellError,
                                            )
                                        }
                                    },
                                    currentSection = dataSetScreenState.currentSection(),
                                    dataSetSections = (dataSetScreenState as DataSetScreenState.Loaded).dataSetSections,
                                    onCellSelected = { cellSelection ->
                                        dataSetTableViewModel.onResizingStatusChanged(cellSelection)
                                    },
                                    currentSelection = tableCellSelection,
                                    onTableResize = dataSetTableViewModel::onTableResize,
                                    emptySectionMessage = stringResource(Res.string.empty_section_message),
                                    emptyDatasetMessage = stringResource(Res.string.empty_dataset_message),
                                    onCheckedCellChanged = { cellId, isChecked ->
                                        dataSetTableViewModel.onUiAction(
                                            UiAction.OnValueChanged(
                                                id = cellId,
                                                newValue =
                                                    when {
                                                        isChecked -> "true"
                                                        else -> null
                                                    },
                                                showInputDialog = false,
                                            ),
                                        )
                                    },
                                )

                            DataSetScreenState.Loading ->
                                ContentLoading(
                                    modifier = Modifier.fillMaxSize(),
                                    MaterialTheme.colorScheme.background,
                                )
                        }
                    },
                    secondaryPane = {
                        if (dataSetScreenState is DataSetScreenState.Loaded) {
                            val tabs by remember {
                                derivedStateOf {
                                    (dataSetScreenState as DataSetScreenState.Loaded).dataSetSections.map { dataSetSection ->
                                        Tab(
                                            id = dataSetSection.uid,
                                            label = dataSetSection.title,
                                        )
                                    }
                                }
                            }
                            VerticalTabs(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = MaterialTheme.colorScheme.background,
                                            shape = RoundedCornerShape(topStart = Radius.L),
                                        ).padding(all = Spacing.Spacing16)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceBright,
                                            shape = RoundedCornerShape(Radius.L),
                                        ).padding(all = Spacing.Spacing0),
                                tabs = tabs,
                                onSectionSelected = { sectionUid ->
                                    dataSetTableViewModel.onSectionSelected(sectionUid)
                                },
                                initialSelectedTabIndex = (dataSetScreenState as DataSetScreenState.Loaded).initialSection,
                            )
                        } else {
                            ContentLoading(
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    },
                )
            } else {
                if (dataSetScreenState is DataSetScreenState.Loaded) {
                    DataSetSinglePane(
                        modifier = Modifier.fillMaxSize(),
                        inputDialogSize = inputDialogSize,
                        fabSize = fabSizeDp,
                        dataSetSections = (dataSetScreenState as DataSetScreenState.Loaded).dataSetSections,
                        dataSetDetails = (dataSetScreenState as DataSetScreenState.Loaded).dataSetDetails,
                        initialTab = (dataSetScreenState as DataSetScreenState.Loaded).initialSection,
                        onSectionSelected = { sectionUid ->
                            dataSetTableViewModel.onSectionSelected(sectionUid)
                        },
                        dataSetSectionTable = (dataSetScreenState as DataSetScreenState.Loaded).dataSetSectionTable,
                        onCellClick = { cellId, cellValue, cellError ->
                            scope.launch {
                                dataSetTableViewModel.updateSelectedCell(
                                    cellId = cellId,
                                    newValue = cellValue,
                                    validationError = cellError,
                                )
                            }
                        },
                        currentSection = dataSetScreenState.currentSection(),
                        currentSelection = tableCellSelection,
                        onCellSelected = { cellSelection ->
                            dataSetTableViewModel.onResizingStatusChanged(cellSelection)
                        },
                        onTableResize = dataSetTableViewModel::onTableResize,
                        emptySectionMessage = stringResource(Res.string.empty_section_message),
                        emptyDatasetMessage = stringResource(Res.string.empty_dataset_message),
                        onCheckedCellChanged = { cellId, isChecked ->
                            dataSetTableViewModel.onUiAction(
                                UiAction.OnValueChanged(
                                    id = cellId,
                                    newValue =
                                        when {
                                            isChecked -> "true"
                                            else -> null
                                        },
                                    showInputDialog = false,
                                ),
                            )
                        },
                    )
                } else {
                    ContentLoading(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                    )
                }
            }

            val selectedCellInfo by remember {
                derivedStateOf {
                    (dataSetScreenState as? DataSetScreenState.Loaded)?.selectedCellInfo
                }
            }

            val visible = selectedCellInfo is CellSelectionState.InputDataUiState

            AnimatedVisibility(
                visible = visible,
                enter =
                    slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                    ),
                exit =
                    slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                    ),
            ) {
                selectedCellInfo?.takeIf { visible }?.let { inputData ->
                    require(inputData is CellSelectionState.InputDataUiState)
                    InputDialog(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .testTag(INPUT_DIALOG_TAG)
                                .onGloballyPositioned { coordinates ->
                                    inputDialogSize = coordinates.size.height
                                },
                        input = {
                            InputProvider(
                                modifier = Modifier,
                                inputData = inputData,
                                onAction = dataSetTableViewModel::onUiAction,
                            )
                        },
                        details = null,
                        actionButton = {
                            Button(
                                style = ButtonStyle.FILLED,
                                text = inputData.buttonAction.buttonText,
                                onClick = {
                                    dataSetTableViewModel.onInputAction(
                                        cellId = inputData.id,
                                        isActionDone = inputData.buttonAction.isDoneAction,
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = inputData.buttonAction.icon,
                                        contentDescription = inputData.buttonAction.buttonText,
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .testTag(inputData.buttonAction.testTag),
                            )
                        },
                        onDismiss = {
                            scope.launch {
                                dataSetTableViewModel.updateSelectedCell(null)
                            }
                        },
                    )
                }
            }

            if (visible.not()) {
                inputDialogSize = null
            }
        }
    }
}

@Composable
private fun nonEditableReasonLabel(edition: DataSetEdition) =
    when (edition.nonEditableReason) {
        is NonEditableReason.AttributeOptionComboNotAssignedToOrgUnit ->
            stringResource(
                Res.string.attribute_option_combo_not_assigned_to_org_unit,
                edition.nonEditableReason.attributeOptionComboLabel,
                edition.nonEditableReason.orgUnitLabel,
            )

        NonEditableReason.Closed ->
            stringResource(Res.string.dataset_closed)

        NonEditableReason.Expired ->
            stringResource(Res.string.dataset_expired)

        is NonEditableReason.NoAttributeOptionComboAccess ->
            stringResource(
                Res.string.attribute_option_combo_no_access,
                edition.nonEditableReason.attributeOptionComboLabel,
            )

        NonEditableReason.NoDataWriteAccess ->
            stringResource(Res.string.no_data_write_access)

        NonEditableReason.None -> ""
        is NonEditableReason.OrgUnitNotInCaptureScope ->
            stringResource(
                Res.string.org_unit_not_in_capture_scope,
                edition.nonEditableReason.orgUnitLabel,
            )

        is NonEditableReason.PeriodIsNotInAttributeOptionComboRange ->
            stringResource(
                Res.string.period_not_in_attribute_option_combo_range,
                edition.nonEditableReason.periodLabel,
                edition.nonEditableReason.attributeOptionComboLabel,
            )

        is NonEditableReason.PeriodIsNotInOrgUnitRange ->
            stringResource(
                Res.string.period_not_in_org_unit_range,
                edition.nonEditableReason.periodLabel,
                edition.nonEditableReason.orgUnitLabel,
            )
    }

/**
 * Data set single pane layout
 * Default layout for portrait devices
 * @param modifier: Modifier for styling
 * @param dataSetSections: List of data set sections
 * @param dataSetDetails: Data set details
 * @param onSectionSelected: Callback function to be invoked when a section is selected
 * */
@Composable
private fun DataSetSinglePane(
    modifier: Modifier = Modifier,
    dataSetSections: List<DataSetSection>,
    inputDialogSize: Int?,
    fabSize: Dp,
    dataSetDetails: DataSetDetails,
    initialTab: Int,
    dataSetSectionTable: DataSetSectionTable,
    emptySectionMessage: String? = null,
    emptyDatasetMessage: String? = null,
    onSectionSelected: (uid: String) -> Unit,
    onCellClick: (
        cellId: String,
        cellValue: String?,
        cellError: String?,
    ) -> Unit,
    onCellSelected: (TableSelection) -> Unit,
    currentSection: String?,
    currentSelection: TableSelection,
    onTableResize: (ResizeAction) -> Unit,
    onCheckedCellChanged: (cellId: String, Boolean) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .clip(
                    shape =
                        RoundedCornerShape(
                            topStart = Radius.L,
                            topEnd = Radius.L,
                        ),
                ).background(
                    color = MaterialTheme.colorScheme.surfaceBright,
                ),
        ) {
            DataSetTable(
                dataSetSectionTable = dataSetSectionTable,
                onCellClick = onCellClick,
                inputDialogSize = inputDialogSize,
                fabSize = fabSize,
                topContent = {
                    Column(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .padding(bottom = Spacing.Spacing24),
                    ) {
                        SectionTabs(
                            modifier = Modifier,
                            dataSetSections = dataSetSections,
                            onSectionSelected = onSectionSelected,
                            selectedTab = initialTab,
                        )
                        DataSetDetails(
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                            dataSetDetails = dataSetDetails,
                        )

                        if (!dataSetSectionTable.loading && dataSetSectionTable.tableModels.isEmpty()) {
                            val message =
                                if (dataSetSections.isEmpty()) {
                                    emptyDatasetMessage
                                } else {
                                    emptySectionMessage
                                }
                            WarningInfoBar(message)
                        }

                        with(
                            dataSetSections.firstOrNull { it.uid == currentSection }?.misconfiguredRows,
                        ) {
                            val message =
                                "Some fields couldn't be displayed due to a configuration issue: %s.\nPlease contact your administrator."
                            val info =
                                when {
                                    this != null && size > 4 -> "${joinToString(", ")} and ${size - 4} more"
                                    this != null -> joinToString(", ")
                                    else -> null
                                }
                            if (!this.isNullOrEmpty()) {
                                WarningInfoBar(message.format(info))
                            }
                        }
                        dataSetSections.firstOrNull { it.uid == currentSection }?.topContent?.let {
                            HtmlContentBox(
                                text = it,
                                modifier =
                                    Modifier.padding(
                                        top = Spacing.Spacing8,
                                        bottom = Spacing.Spacing0,
                                        start = Spacing.Spacing16,
                                        end = Spacing.Spacing16,
                                    ),
                            )
                        }
                    }
                },
                bottomContent = {
                    dataSetSections.firstOrNull { it.uid == currentSection }?.bottomContent?.let {
                        HtmlContentBox(
                            text = it,
                            modifier =
                                Modifier.padding(
                                    top = Spacing.Spacing24,
                                    start = Spacing.Spacing16,
                                    end = Spacing.Spacing16,
                                ),
                        )
                    }
                },
                onCellSelected = onCellSelected,
                currentSelection = currentSelection,
                onTableResize = onTableResize,
                loading = dataSetSectionTable.loading,
                onCheckedCellChanged = onCheckedCellChanged,
            )
        }
    }
}

@Composable
private fun SectionTabs(
    modifier: Modifier = Modifier,
    dataSetSections: List<DataSetSection>,
    selectedTab: Int,
    onSectionSelected: (uid: String) -> Unit,
) {
    val tabLabels =
        remember {
            dataSetSections.map { it.title }
        }
    AdaptiveTabRow(
        modifier =
            modifier
                .height(Spacing.Spacing48)
                .fillMaxWidth()
                .background(Color.Transparent),
        tabLabels = tabLabels,
        selectedTab = selectedTab,
        onTabClicked = { selectedTabIndex ->
            onSectionSelected(dataSetSections[selectedTabIndex].uid)
        },
    )
}

@Composable
private fun DataSetTableContent(
    modifier: Modifier = Modifier,
    dataSetSections: List<DataSetSection>,
    dataSetDetails: DataSetDetails,
    dataSetSectionTable: DataSetSectionTable,
    inputDialogSize: Int? = null,
    fabSize: Dp,
    currentSection: String?,
    emptySectionMessage: String? = null,
    emptyDatasetMessage: String? = null,
    onCellClick: (
        cellId: String,
        cellValue: String?,
        cellError: String?,
    ) -> Unit,
    onCellSelected: (TableSelection) -> Unit,
    currentSelection: TableSelection,
    onTableResize: (ResizeAction) -> Unit,
    onCheckedCellChanged: (cellId: String, Boolean) -> Unit,
) {
    Box(
        modifier = modifier,
    ) {
        DataSetTable(
            dataSetSectionTable = dataSetSectionTable,
            inputDialogSize = inputDialogSize,
            fabSize = fabSize,
            onCellClick = onCellClick,
            topContent = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val dataSetDetailsBottomPadding =
                        if (dataSetSections.firstOrNull { it.uid == currentSection }?.topContent != null) {
                            Spacing.Spacing24
                        } else {
                            Spacing.Spacing8
                        }
                    DataSetDetails(
                        modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = Radius.L,
                                    topEnd = Radius.L,
                                ),
                            ).padding(
                                start = Spacing.Spacing16,
                                end = Spacing.Spacing16,
                                bottom = dataSetDetailsBottomPadding,
                            ).animateContentSize(),
                        dataSetDetails = dataSetDetails,
                    )

                    if (!dataSetSectionTable.loading && dataSetSectionTable.tableModels.isEmpty()) {
                        val message =
                            if (dataSetSections.isEmpty()) {
                                emptyDatasetMessage
                            } else {
                                emptySectionMessage
                            }
                        WarningInfoBar(message)
                    }

                    dataSetSections.firstOrNull { it.uid == currentSection }?.topContent?.let {
                        HtmlContentBox(
                            text = it,
                            modifier =
                                Modifier.padding(
                                    bottom = Spacing.Spacing8,
                                    start = Spacing.Spacing16,
                                    end = Spacing.Spacing16,
                                ),
                        )
                    }
                }
            },
            bottomContent = {
                dataSetSections.firstOrNull { it.uid == currentSection }?.bottomContent?.let {
                    HtmlContentBox(
                        text = it,
                        modifier =
                            Modifier
                                .padding(
                                    top = Spacing.Spacing24,
                                    start = Spacing.Spacing16,
                                    end = Spacing.Spacing16,
                                ).testTag("HTML_BOTTOM_CONTENT"),
                    )
                }
            },
            onCellSelected = onCellSelected,
            currentSelection = currentSelection,
            onTableResize = onTableResize,
            loading = dataSetSectionTable.loading,
            onCheckedCellChanged = onCheckedCellChanged,
        )
    }
}

@Composable
private fun ContentLoading(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceBright,
) {
    Box(
        modifier = modifier.fillMaxWidth().background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        ProgressIndicator(type = ProgressIndicatorType.CIRCULAR)
    }
}

@Composable
private fun WarningInfoBar(message: String?) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = Spacing.Spacing24, start = Spacing.Spacing16, end = Spacing.Spacing16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        InfoBar(
            text = message ?: "",
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

@Composable
private fun DataSetTable(
    dataSetSectionTable: DataSetSectionTable,
    currentSelection: TableSelection,
    inputDialogSize: Int?,
    fabSize: Dp,
    topContent: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null,
    onCellClick: (
        cellId: String,
        cellValue: String?,
        cellError: String?,
    ) -> Unit,
    onCellSelected: (TableSelection) -> Unit,
    onCheckedCellChanged: (cellId: String, Boolean) -> Unit,
    onTableResize: (ResizeAction) -> Unit,
    loading: Boolean,
) {
    val density = LocalDensity.current

    val inputDialogSizeDp =
        with(density) {
            inputDialogSize?.toDp() ?: 0.dp
        }

    val screenWidth = with(density) { getScreenWidth().roundToPx() - Spacing.Spacing32.roundToPx() }
    val sectionTableId = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(dataSetSectionTable) {
        sectionTableId.value = dataSetSectionTable.id
    }

    val tableResizeActions =
        remember {
            object : TableResizeActions {
                override fun onRowHeaderResize(
                    tableId: String,
                    newValue: Float,
                ) {
                    sectionTableId.value?.let {
                        onTableResize(
                            ResizeAction.RowHeaderChanged(
                                tableId,
                                sectionTableId.value ?: "",
                                newValue,
                            ),
                        )
                    }
                }

                override fun onColumnHeaderResize(
                    tableId: String,
                    column: Int,
                    newValue: Float,
                ) {
                    sectionTableId.value?.let {
                        onTableResize(
                            ResizeAction.ColumnHeaderChanged(
                                tableId,
                                sectionTableId.value ?: "",
                                column,
                                newValue,
                            ),
                        )
                    }
                }

                override fun onTableDimensionResize(
                    tableId: String,
                    newValue: Float,
                ) {
                    sectionTableId.value?.let {
                        onTableResize(
                            ResizeAction.TableDimension(tableId, sectionTableId.value ?: "", newValue),
                        )
                    }
                }

                override fun onTableDimensionReset(tableId: String) {
                    sectionTableId.value?.let {
                        onTableResize(
                            ResizeAction.Reset(tableId, sectionTableId.value ?: ""),
                        )
                    }
                }
            }
        }
    TableTheme(
        tableDimensions =
            dataSetSectionTable.overridingDimensions.let { overwrittenDimension ->
                TableDimensions(
                    extraWidths =
                        with(density) {
                            overwrittenDimension.overwrittenTableWidth.mapValues { (_, width) ->
                                width.dp.roundToPx()
                            }
                        },
                    rowHeaderWidths =
                        with(density) {
                            overwrittenDimension.overwrittenRowHeaderWidth.mapValues { (_, width) ->
                                width.dp.roundToPx()
                            }
                        },
                    columnWidth =
                        with(density) {
                            overwrittenDimension.overwrittenColumnWidth.mapValues { (_, value) ->
                                value.mapValues { (_, width) ->
                                    width.dp.roundToPx()
                                }
                            }
                        },
                    totalWidth = screenWidth,
                )
            },
    ) {
        DataTable(
            tableList = dataSetSectionTable.tableModels,
            currentSelection = currentSelection,
            onResizedActions = tableResizeActions,
            tableInteractions =
                object : TableInteractions {
                    override fun onClick(tableCell: TableCell) {
                        super.onClick(tableCell)
                        onCellClick(tableCell.id, tableCell.value, tableCell.error)
                    }

                    override fun onSelectionChange(newTableSelection: TableSelection) {
                        super.onSelectionChange(newTableSelection)
                        if (newTableSelection !is TableSelection.CellSelection) {
                            onCellSelected(newTableSelection)
                        }
                    }

                    override fun onChecked(
                        tableCell: TableCell,
                        checked: Boolean,
                    ) {
                        super.onChecked(tableCell, checked)
                        onCheckedCellChanged(tableCell.id, checked)
                    }
                },
            topContent = topContent,
            bottomContent = bottomContent,
            contentPadding =
                PaddingValues(
                    start = Spacing.Spacing16,
                    top = Spacing.Spacing8,
                    end = Spacing.Spacing16,
                    bottom = Spacing.Spacing16 + inputDialogSizeDp + fabSize,
                ),
            loading = loading,
        )
    }
}

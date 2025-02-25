@file:OptIn(ExperimentalMaterial3Api::class)

package org.dhis2.mobile.aggregates.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.dhis2.mobile.aggregates.resources.Res
import org.dhis2.mobile.aggregates.resources.action_done
import org.dhis2.mobile.aggregates.ui.component.HtmlContentBox
import org.dhis2.mobile.aggregates.ui.constants.INPUT_DIALOG_DONE_TAG
import org.dhis2.mobile.aggregates.ui.constants.INPUT_DIALOG_TAG
import org.dhis2.mobile.aggregates.ui.constants.SYNC_BUTTON_TAG
import org.dhis2.mobile.aggregates.ui.inputs.InputProvider
import org.dhis2.mobile.aggregates.ui.component.ValidationBar
import org.dhis2.mobile.aggregates.ui.states.DataSetScreenState
import org.dhis2.mobile.aggregates.ui.states.DataSetSectionTable
import org.dhis2.mobile.aggregates.ui.viewModel.DataSetTableViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.FAB
import org.hisp.dhis.mobile.ui.designsystem.component.FABStyle
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
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
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.DataTable
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.TableSelection
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.jetbrains.compose.resources.stringResource
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
    onBackClicked: () -> Unit,
    onSyncClicked: () -> Unit,
) {
    val dataSetTableViewModel: DataSetTableViewModel =
        koinViewModel<DataSetTableViewModel>(parameters = {
            parametersOf(
                parameters.dataSetUid,
                parameters.periodId,
                parameters.organisationUnitUid,
                parameters.attributeOptionComboUid,
                onBackClicked,
            )
        })
    val dataSetScreenState by dataSetTableViewModel.dataSetScreenState.collectAsState()

    val allowTwoPane by remember(useTwoPane, dataSetScreenState) {
        derivedStateOf {
            dataSetScreenState.allowTwoPane(useTwoPane)
        }
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    scope.launch {
        dataSetScreenState.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.primary,
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
                        onClick = onSyncClicked,
                    )
                },
                title = {
                    if (dataSetScreenState is DataSetScreenState.Loaded) {
                        Text(
                            text = (dataSetScreenState as DataSetScreenState.Loaded).dataSetDetails.titleLabel,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        snackbarHost = { CustomSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = ((dataSetScreenState as? DataSetScreenState.Loaded)?.dataSetSectionTable is DataSetSectionTable.Loaded),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                FAB(
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
            (dataSetScreenState as? DataSetScreenState.Loaded)?.validationBar?.let { validationBarUiState ->
                ValidationBar(uiState = validationBarUiState)
            }
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(it),
            propagateMinConstraints = true,
        ) {
            if (allowTwoPane) {
                TwoPaneLayout(
                    modifier = Modifier.fillMaxSize()
                        .background(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(topStart = Radius.L, topEnd = Radius.L),
                        ),
                    paneConfig = TwoPaneConfig.SecondaryPaneFixedSize(283.dp),
                    primaryPane = {
                        when (dataSetScreenState) {
                            is DataSetScreenState.Loaded ->
                                DataSetTableContent(
                                    modifier = Modifier.background(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = RoundedCornerShape(topEnd = Radius.L),
                                    ),
                                    dataSetDetails = (dataSetScreenState as DataSetScreenState.Loaded).dataSetDetails,
                                    dataSetSectionTable = (dataSetScreenState as DataSetScreenState.Loaded).dataSetSectionTable,
                                    onCellClick = dataSetTableViewModel::updateSelectedCell,
                                    currentSection = dataSetScreenState.currentSection(),
                                    dataSetSections = (dataSetScreenState as DataSetScreenState.Loaded).dataSetSections,
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = RoundedCornerShape(topStart = Radius.L),
                                    )
                                    .padding(all = Spacing.Spacing16)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceBright,
                                        shape = RoundedCornerShape(Radius.L),
                                    )
                                    .padding(all = Spacing.Spacing0),
                                tabs = tabs,
                                onSectionSelected = dataSetTableViewModel::onSectionSelected,
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
                        dataSetSections = (dataSetScreenState as DataSetScreenState.Loaded).dataSetSections,
                        dataSetDetails = (dataSetScreenState as DataSetScreenState.Loaded).dataSetDetails,
                        onSectionSelected = dataSetTableViewModel::onSectionSelected,
                        dataSetSectionTable = (dataSetScreenState as DataSetScreenState.Loaded).dataSetSectionTable,
                        onCellClick = dataSetTableViewModel::updateSelectedCell,
                        currentSection = dataSetScreenState.currentSection(),
                    )
                } else {
                    ContentLoading(
                        modifier = Modifier.fillMaxSize().padding(it),
                    )
                }
            }

            val selectedCellInfo =
                (dataSetScreenState as? DataSetScreenState.Loaded)?.selectedCellInfo

            AnimatedVisibility(
                visible = selectedCellInfo != null,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                ),
            ) {
                InputDialog(
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .testTag(INPUT_DIALOG_TAG),
                    input = {
                        selectedCellInfo?.let { inputData ->
                            InputProvider(
                                modifier = Modifier,
                                inputData = inputData,
                                onAction = dataSetTableViewModel::onUiAction,
                            )
                        }
                    },
                    details = null,
                    actionButton = {
                        Button(
                            style = ButtonStyle.FILLED,
                            text = stringResource(Res.string.action_done),
                            onClick = {
                                dataSetTableViewModel.updateSelectedCell(null)
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Done",
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                                .testTag(INPUT_DIALOG_DONE_TAG),
                        )
                    },
                    onDismiss = {
                        dataSetTableViewModel.updateSelectedCell(null)
                    },
                )
            }
        }
    }

    (dataSetScreenState as? DataSetScreenState.Loaded)?.modalDialog?.let { dataSetUIState ->
        BottomSheetShell(
            uiState = dataSetUIState.contentDialogUIState,
            content = dataSetUIState.content,
            buttonBlock = dataSetUIState.buttonsDialog,
            onDismiss = dataSetUIState.onDismiss,
            icon = dataSetUIState.icon,
        )
    }
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
    dataSetDetails: DataSetDetails,
    dataSetSectionTable: DataSetSectionTable,
    onSectionSelected: (uid: String) -> Unit,
    onCellClick: (cellId: String) -> Unit,
    currentSection: String?,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.primary,
            )
            .clip(
                RoundedCornerShape(
                    topStart = Radius.L,
                    topEnd = Radius.L,
                ),
            ),
    ) {
        SectionTabs(
            dataSetSections = dataSetSections,
            onSectionSelected = onSectionSelected,
        )

        DataSetDetails(
            modifier = Modifier
                .height(68.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(
                        topStart = Radius.L,
                        topEnd = Radius.L,
                    ),
                ).padding(horizontal = Spacing.Spacing16),
            dataSetDetails = dataSetDetails,
        )

        dataSetSections.firstOrNull { it.uid == currentSection }?.topContent?.let {
            HtmlContentBox(
                text = it,
                modifier = Modifier.padding(bottom = Spacing.Spacing8, start = Spacing.Spacing16, end = Spacing.Spacing16),
            )
        }

        when (dataSetSectionTable) {
            is DataSetSectionTable.Loaded ->
                DataSetTable(
                    tableModels = dataSetSectionTable.tables(),
                    onCellClick = onCellClick,
                    bottomContent = {
                        dataSetSections.firstOrNull { it.uid == currentSection }?.bottomContent?.let {
                            HtmlContentBox(
                                text = it,
                                modifier = Modifier.padding(top = Spacing.Spacing24, start = Spacing.Spacing0, end = Spacing.Spacing0),

                            )
                        }
                    },
                )

            DataSetSectionTable.Loading ->
                ContentLoading(Modifier.weight(1f))
        }
    }
}

@Composable
private fun SectionTabs(
    modifier: Modifier = Modifier,
    dataSetSections: List<DataSetSection>,
    onSectionSelected: (uid: String) -> Unit,
) {
    val tabLabels = remember {
        dataSetSections.map { it.title }
    }
    AdaptiveTabRow(
        modifier = modifier
            .height(Spacing.Spacing48)
            .fillMaxWidth(),
        tabLabels = tabLabels,
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
    currentSection: String?,
    onCellClick: (cellId: String) -> Unit,
) {
    Column(
        modifier = modifier
            .padding(horizontal = Spacing.Spacing16, vertical = Spacing.Spacing24),
        verticalArrangement = spacedBy(Spacing.Spacing24),
    ) {
        DataSetDetails(
            modifier.padding(horizontal = Spacing.Spacing16, vertical = Spacing.Spacing24),
            dataSetDetails = dataSetDetails,
        )
        dataSetSections.firstOrNull { it.uid == currentSection }?.topContent?.let {
            HtmlContentBox(
                text = it,
                modifier = Modifier.padding(bottom = Spacing.Spacing8, start = Spacing.Spacing16, end = Spacing.Spacing16),
            )
        }

        when (dataSetSectionTable) {
            is DataSetSectionTable.Loaded ->
                DataSetTable(
                    tableModels = dataSetSectionTable.tables(),
                    onCellClick = onCellClick,
                    bottomContent = {
                        dataSetSections.firstOrNull { it.uid == currentSection }?.bottomContent?.let {
                            HtmlContentBox(
                                text = it,
                                modifier = Modifier.padding(top = Spacing.Spacing24, start = Spacing.Spacing0, end = Spacing.Spacing0).testTag("HTML_BOTTOM_CONTENT"),
                            )
                        }
                    },
                )

            DataSetSectionTable.Loading ->
                ContentLoading(Modifier.weight(1f))
        }
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
private fun DataSetTable(
    tableModels: List<TableModel>,
    currentSelection: TableSelection = TableSelection.Unselected(),
    bottomContent: @Composable (() -> Unit)? = null,
    onCellClick: (cellId: String) -> Unit,
) {
    DataTable(
        tableList = tableModels,
        currentSelection = currentSelection,
        tableInteractions = object : TableInteractions {
            override fun onClick(tableCell: TableCell) {
                super.onClick(tableCell)
                onCellClick(tableCell.id)
            }

            override fun onSelectionChange(newTableSelection: TableSelection) {
                super.onSelectionChange(newTableSelection)
            }
        },
        bottomContent = bottomContent,
    )
}

@Composable
private fun CustomSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState = hostState) { data ->
        Snackbar(
            modifier = Modifier.dropShadow(shape = SnackbarDefaults.shape),
            snackbarData = data,
            containerColor = SurfaceColor.SurfaceBright,
            contentColor = TextColor.OnSurface,
        )
    }
}

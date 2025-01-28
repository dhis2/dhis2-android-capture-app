@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package org.dhis2.mobile.aggregates.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.dhis2.mobile.aggregates.ui.states.ScreenState
import org.dhis2.mobile.aggregates.ui.viewModel.DataSetTableViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.component.TopBar
import org.hisp.dhis.mobile.ui.designsystem.component.TopBarActionIcon
import org.hisp.dhis.mobile.ui.designsystem.component.TopBarDropdownMenuIcon
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Data set table screen
 * Shows the data set details and tables
 * @param parameters: Data set instance parameters
 * @param useTwoPane: Whether to use a two pane layout
 * @param onBackClicked: Callback function to be invoked when the back button is clicked
 * */
@Composable
fun DataSetInstanceScreen(
    parameters: DataSetInstanceParameters,
    useTwoPane: Boolean,
    onBackClicked: () -> Unit,
) {
    val dataSetTableViewModel: DataSetTableViewModel =
        koinViewModel<DataSetTableViewModel>(parameters = {
            parametersOf(
                parameters.dataSetUid,
                parameters.periodId,
                parameters.organisationUnitUid,
                parameters.attributeOptionComboUid,
            )
        })
    val dataSetScreenState by dataSetTableViewModel.dataSetScreenState.collectAsState()
    val onSectionSelected: (uid: String) -> Unit = {}
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
                        onClick = onBackClicked,
                    )
                },
                actions = {
                    TopBarDropdownMenuIcon(
                        iconTint = MaterialTheme.colorScheme.onPrimary,
                        dropDownMenu = { showMenu, onDismissRequest ->
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = onDismissRequest,
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Action 1") },
                                    onClick = {},
                                    leadingIcon = {
                                        IconButton(
                                            onClick = {
                                                onDismissRequest()
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = "Edit Button",
                                                )
                                            },
                                        )
                                    },
                                )
                            }
                        },
                    )
                },
                title = {
                    if (dataSetScreenState is ScreenState.DataSetScreenState) {
                        Text(
                            (dataSetScreenState as ScreenState.DataSetScreenState).dataSetDetails.titleLabel,
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
    ) {
        if (useTwoPane) {
            TwoPaneScreen(
                modifier = Modifier.fillMaxSize().padding(it),
                primaryPaneWeight = 0.7f,
                primaryPane = {
                    if (dataSetScreenState is ScreenState.DataSetScreenState) {
                        DataSetTableContent(
                            dataSetDetails = (dataSetScreenState as ScreenState.DataSetScreenState).dataSetDetails,
                        )
                    }
                },
                secondaryPane = {
                    if (dataSetScreenState is ScreenState.DataSetScreenState) {
                        SectionColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    top = 0.dp,
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 16.dp,
                                ),
                            dataSetSections = (dataSetScreenState as ScreenState.DataSetScreenState).dataSetSections,
                            onSectionSelected = onSectionSelected,
                        )
                    } else {
                        ProgressIndicator(type = ProgressIndicatorType.CIRCULAR)
                    }
                },
            )
        } else {
            if (dataSetScreenState is ScreenState.DataSetScreenState) {
                DataSetSinglePane(
                    modifier = Modifier.fillMaxSize().padding(it),
                    dataSetSections = (dataSetScreenState as ScreenState.DataSetScreenState).dataSetSections,
                    dataSetDetails = (dataSetScreenState as ScreenState.DataSetScreenState).dataSetDetails,
                    onSectionSelected = onSectionSelected,
                )
            } else {
                ProgressIndicator(type = ProgressIndicatorType.CIRCULAR)
            }
        }
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DataSetSinglePane(
    modifier: Modifier = Modifier,
    dataSetSections: List<DataSetSection>,
    dataSetDetails: DataSetDetails,
    onSectionSelected: (uid: String) -> Unit,
) {
    LazyColumn(
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
        item(key = "section_tabs") {
            SectionTabs(
                dataSetSections = dataSetSections,
                onSectionSelected = onSectionSelected,
            )
        }

        item(key = "details") {
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
                    ).padding(horizontal = 16.dp),
                dataSetDetails = dataSetDetails,
            )
        }

        repeat(10) {
            stickyHeader {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    text = "Table $it",
                )
            }
            item {
                DataSetTables(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.background)
                        .height(200.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                )
            }
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
            .height(48.dp)
            .fillMaxWidth(),
        tabLabels = tabLabels,
        onTabClicked = { selectedTabIndex ->
            onSectionSelected(dataSetSections[selectedTabIndex].uid)
        },
    )
}

@Composable
private fun SectionColumn(
    modifier: Modifier = Modifier,
    dataSetSections: List<DataSetSection>,
    onSectionSelected: (String) -> Unit,
) {
    var selectedSection by remember { mutableStateOf(0) }
    val indicatorVerticalOffset by animateDpAsState(
        targetValue = (selectedSection * 48).dp,
        label = "",
    )

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
            ),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(dataSetSections) { index, item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable {
                            selectedSection = index
                            onSectionSelected(item.uid)
                        },
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterStart)
                            .padding(horizontal = 16.dp),
                        text = item.title,
                        color = if (selectedSection == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
        Spacer(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 3.dp, y = indicatorVerticalOffset)
                .requiredHeight(48.dp)
                .requiredWidth(6.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(3.0.dp),
                ),
        )
    }
}

@Composable
private fun DataSetTableContent(
    modifier: Modifier = Modifier,
    dataSetDetails: DataSetDetails,
) {
    LazyColumn(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(topStart = Radius.L, topEnd = Radius.L),
            )
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = spacedBy(Spacing.Spacing24),
    ) {
        item {
            DataSetDetails(
                modifier.padding(horizontal = 16.dp),
                dataSetDetails = dataSetDetails,
            )
        }

        items(count = 10) {
            DataSetTables(
                modifier = Modifier.height(200.dp).fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DataSetTables(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small,
            ),
    ) { }
}

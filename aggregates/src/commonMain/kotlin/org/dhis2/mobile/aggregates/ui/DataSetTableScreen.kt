@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package org.dhis2.mobile.aggregates.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.dhis2.mobile.aggregates.ui.states.DataSetScreenState
import org.dhis2.mobile.aggregates.ui.states.DataSetSectionTable
import org.dhis2.mobile.aggregates.ui.viewModel.DataSetTableViewModel
import org.dhis2.mobile.commons.html.HtmlStyle
import org.dhis2.mobile.commons.html.htmlToAnnotatedString
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.component.TopBar
import org.hisp.dhis.mobile.ui.designsystem.component.TopBarActionIcon
import org.hisp.dhis.mobile.ui.designsystem.component.TopBarDropdownMenuIcon
import org.hisp.dhis.mobile.ui.designsystem.component.VerticalTabs
import org.hisp.dhis.mobile.ui.designsystem.component.layout.TwoPaneConfig
import org.hisp.dhis.mobile.ui.designsystem.component.layout.TwoPaneLayout
import org.hisp.dhis.mobile.ui.designsystem.component.model.Tab
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.DataTable
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
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

    val allowTwoPane by remember(useTwoPane, dataSetScreenState) {
        derivedStateOf {
            dataSetScreenState.allowTwoPane(useTwoPane)
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
    ) {
        if (allowTwoPane) {
            TwoPaneLayout(
                modifier = Modifier.fillMaxSize()
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(topStart = Radius.L, topEnd = Radius.L),
                    )
                    .padding(it),
                paneConfig = TwoPaneConfig.SecondaryPaneFixedSize(283.dp),
                primaryPane = {
                    when (dataSetScreenState) {
                        is DataSetScreenState.Loaded ->

                            DataSetTableContent(
                                modifier = Modifier.background(
                                    color = MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(topEnd = Radius.L),
                                ),
                                dataSetSections = (dataSetScreenState as DataSetScreenState.Loaded).dataSetSections,
                                dataSetDetails = (dataSetScreenState as DataSetScreenState.Loaded).dataSetDetails,
                                dataSetSectionTable = (dataSetScreenState as DataSetScreenState.Loaded).dataSetSectionTable,
                                currentSection = dataSetScreenState.currentSection(),
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
                    modifier = Modifier.fillMaxSize().padding(it),
                    dataSetSections = (dataSetScreenState as DataSetScreenState.Loaded).dataSetSections,
                    dataSetDetails = (dataSetScreenState as DataSetScreenState.Loaded).dataSetDetails,
                    onSectionSelected = dataSetTableViewModel::onSectionSelected,
                    dataSetSectionTable = (dataSetScreenState as DataSetScreenState.Loaded).dataSetSectionTable,
                    currentSection = dataSetScreenState.currentSection(),
                )
            } else {
                ContentLoading(
                    modifier = Modifier.fillMaxSize().padding(it),
                )
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
@Composable
private fun DataSetSinglePane(
    modifier: Modifier = Modifier,
    dataSetSections: List<DataSetSection>,
    dataSetDetails: DataSetDetails,
    dataSetSectionTable: DataSetSectionTable,
    onSectionSelected: (uid: String) -> Unit,
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
                DataSetTable(dataSetSectionTable.tables(), bottomContent = {
                    dataSetSections.firstOrNull { it.uid == currentSection }?.bottomContent?.let {
                        HtmlContentBox(
                            text = it,
                            modifier = Modifier.padding(top = Spacing.Spacing24, start = Spacing.Spacing0, end = Spacing.Spacing0),

                        )
                    }
                })

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

) {
    Column(
        modifier = modifier
            .padding(horizontal = Spacing.Spacing16, vertical = Spacing.Spacing24),

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
                DataSetTable(dataSetSectionTable.tables(), bottomContent = {
                    dataSetSections.firstOrNull { it.uid == currentSection }?.bottomContent?.let {
                        HtmlContentBox(
                            text = it,
                            modifier = Modifier.padding(top = Spacing.Spacing24, start = Spacing.Spacing0, end = Spacing.Spacing0).testTag("HTML_BOTTOM_CONTENT"),

                        )
                    }
                })

            DataSetSectionTable.Loading ->
                ContentLoading(Modifier.weight(1f))
        }
    }
}

@Composable
fun HtmlContentBox(text: String, modifier: Modifier = Modifier) {
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = TextColor.OnSurfaceLight,
    )
    val formatedText = htmlToAnnotatedString(
        html = text,
        linkStyle = HtmlStyle(
            textLinkStyles = TextLinkStyles(
                style = textStyle.toSpanStyle().copy(color = SurfaceColor.Primary, textDecoration = TextDecoration.Underline),
            ),
        ),
        genericStyle = textStyle,
    )
    Column(Modifier.fillMaxWidth().background(color = SurfaceColor.ContainerLowest)) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = SurfaceColor.ContainerLow,
                    shape = Shape.Small,
                ),
            contentAlignment = Alignment.CenterStart,
        ) {
            Column(Modifier.padding(Spacing.Spacing8)) {
                Text(
                    text = formatedText,
                    textAlign = TextAlign.Start,
                )
            }
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
private fun DataSetTable(tableModels: List<TableModel>, bottomContent: @Composable (() -> Unit)? = null) {
    DataTable(
        tableList = tableModels,
        bottomContent = bottomContent,
    )
}

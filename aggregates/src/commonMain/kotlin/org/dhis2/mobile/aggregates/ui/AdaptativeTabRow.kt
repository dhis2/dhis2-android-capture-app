package org.dhis2.mobile.aggregates.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

private const val MAX_LABEL_LENGTH = 40

/**
 * Adaptive Tab Row
 * Displays a scrollable tab row if the total width of the tabs exceeds the screen width.
 * @param modifier Modifier for styling
 * @param tabLabels List of tab labels to display
 * @param onTabClicked Callback function to be invoked when a tab is clicked
 * **/
@Composable
fun AdaptiveTabRow(
    modifier: Modifier = Modifier,
    tabLabels: List<String>,
    selectedTab: Int,
    onTabClicked: (index: Int) -> Unit,
) {
    if (tabLabels.size <= 1) return

    var currentSelectedTab by remember { mutableStateOf(selectedTab) }
    val tabWidths = remember { mutableStateListOf<Int>() }
    var scrollable by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val indicatorWidth by animateDpAsState(
        targetValue =
            with(density) {
                tabWidths.getOrNull(currentSelectedTab)?.toDp()
            } ?: 56.dp,
        label = "indicator_width",
    )

    SubcomposeLayout(modifier = modifier) { constraints ->

        val tabPlaceables =
            subcompose("tabs") {
                tabLabels.forEachIndexed { index, tabLabel ->
                    AdaptiveTab(
                        index = index,
                        tabLabel = tabLabel,
                        tabWidths = tabWidths,
                        isSelected = currentSelectedTab == index,
                        onClick = {},
                    )
                }
            }.map { measurable ->
                measurable.measure(constraints)
            }

        val totalTabWidth = tabPlaceables.sumOf { it.width }
        val screenWidth = constraints.maxWidth
        scrollable = totalTabWidth > screenWidth

        val finalPlaceable =
            subcompose("finalLayout") {
                if (scrollable) {
                    ScrollableTabRow(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .semantics {
                                    testTag = "SCROLLABLE_TAB_ROW"
                                },
                        selectedTabIndex = currentSelectedTab,
                        containerColor = MaterialTheme.colorScheme.primary,
                        edgePadding = Spacing.Spacing16,
                        indicator = { tabPositions ->
                            TabRowDefaults.PrimaryIndicator(
                                width = indicatorWidth,
                                modifier = Modifier.tabIndicatorOffset(tabPositions[currentSelectedTab]),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        },
                        divider = {},
                    ) {
                        tabLabels.forEachIndexed { index, tabLabel ->
                            AdaptiveTab(
                                modifier = Modifier.testTag("SCROLLABLE_TAB_$index"),
                                index = index,
                                tabLabel = tabLabel,
                                tabWidths = tabWidths,
                                isSelected = currentSelectedTab == index,
                                onClick = {
                                    currentSelectedTab = index
                                    onTabClicked(index)
                                },
                            )
                        }
                    }
                } else {
                    TabRow(
                        modifier =
                            modifier
                                .height(48.dp)
                                .fillMaxWidth()
                                .semantics {
                                    testTag = "TAB_ROW"
                                },
                        selectedTabIndex = currentSelectedTab,
                        containerColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.PrimaryIndicator(
                                width = indicatorWidth,
                                modifier = Modifier.tabIndicatorOffset(tabPositions[currentSelectedTab]),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        },
                        divider = {},
                    ) {
                        tabLabels.forEachIndexed { index, tabLabel ->
                            AdaptiveTab(
                                modifier = Modifier.testTag("TAB_$index"),
                                index = index,
                                tabLabel = tabLabel,
                                tabWidths = tabWidths,
                                isSelected = currentSelectedTab == index,
                                onClick = {
                                    currentSelectedTab = index
                                    onTabClicked(index)
                                },
                            )
                        }
                    }
                }
            }.map { measurable ->
                measurable.measure(constraints)
            }

        layout(constraints.maxWidth, 48.dp.roundToPx()) {
            finalPlaceable.forEach { placeable ->
                placeable.place(0, 0)
            }
        }
    }
}

@Composable
private fun AdaptiveTab(
    modifier: Modifier = Modifier,
    index: Int,
    tabLabel: String,
    maxLength: Int = MAX_LABEL_LENGTH,
    tabWidths: MutableList<Int>,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Tab(
        modifier =
            modifier
                .height(48.dp)
                .padding(horizontal = Spacing.Spacing32)
                .onGloballyPositioned { coordinates ->
                    tabWidths.add(index, coordinates.size.width)
                },
        selected = isSelected,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick,
    ) {
        Text(
            text =
                if (tabLabel.length > maxLength) {
                    tabLabel.take(maxLength) + "…"
                } else {
                    tabLabel
                },
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

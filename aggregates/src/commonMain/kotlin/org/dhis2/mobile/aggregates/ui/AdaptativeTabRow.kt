package org.dhis2.mobile.aggregates.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

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
    onTabClicked: (index: Int) -> Unit,
) {
    if (tabLabels.isEmpty()) return

    var selectedTab by remember { mutableStateOf(0) }
    val tabWidths = remember { mutableStateListOf<Int>() }
    var scrollable by remember { mutableStateOf(false) }

    // Calculate total width of tabs
    val totalTabWidth = tabWidths.sum()
    val screenWidth = with(LocalDensity.current) {
        getScreenWidth().roundToPx()
    }

    LaunchedEffect(key1 = totalTabWidth, key2 = screenWidth) {
        // Determine if tabs should be scrollable
        scrollable = totalTabWidth > screenWidth
    }

    // TabRow with conditional behavior
    if (false) {
        ScrollableTabRow(
            modifier = modifier
                .height(48.dp)
                .fillMaxWidth(),
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.primary,
            edgePadding = Spacing.Spacing16,
            indicator = { tabPositions ->
                TabRowDefaults.PrimaryIndicator(
                    width = 56.dp,
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            },
            divider = {},
        ) {
            tabLabels.forEachIndexed { index, tabLabel ->
                Tab(
                    modifier = Modifier
                        .height(48.dp)
                        .onGloballyPositioned { coordinates ->
                            tabWidths.add(index, coordinates.size.width)
                        },
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        onTabClicked(index)
                    },
                ) {
                    Text(
                        text = tabLabel,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
    } else {
        TabRow(
            modifier = modifier
                .height(48.dp)
                .fillMaxWidth(),
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.PrimaryIndicator(
                    width = 56.dp,
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            },
            divider = {},
        ) {
            tabLabels.forEachIndexed { index, tabLabel ->
                Tab(
                    modifier = Modifier
                        .height(48.dp)
                        .onGloballyPositioned { coordinates ->
                            tabWidths.add(index, coordinates.size.width)
                        },
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        onTabClicked(index)
                    },
                ) {
                    Text(
                        text = tabLabel,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
    }
}

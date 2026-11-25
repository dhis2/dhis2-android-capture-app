package org.dhis2.mobile.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRailColors
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailItemDefaults
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dhis2.mobile.home.resources.Res
import org.dhis2.mobile.home.resources.ic_dhis_logo
import org.hisp.dhis.mobile.ui.designsystem.theme.Outline
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

/**
 * Navigation menu of the home screen, shown as a modal wide navigation rail that is hidden while
 * collapsed. It is used on every window size, phones and tablets alike.
 *
 * @param selectedItem the item to mark as selected, or null for none. It is controlled by the
 *   caller because the current screen can also change without going through this menu.
 * @param onItemSelected called when an item is clicked; the rail collapses afterwards.
 */
@Composable
fun ModalHomeNavigationRail(
    serverName: String,
    userName: String,
    displayDeveloperOptions: Boolean,
    selectedItem: HomeNavigationReelItem?,
    state: WideNavigationRailState = rememberWideNavigationRailState(),
    onItemSelected: (HomeNavigationReelItem) -> Unit,
) {
    val scope = rememberCoroutineScope()

    ModalWideNavigationRail(
        state = state,
        hideOnCollapse = true,
        colors = homeNavigationRailColors(),
        // The header draws behind the status bar itself, so the rail must not add the insets.
        windowInsets = WindowInsets(0),
        header = {
            HomeNavigationReelHeader(serverName, userName)
        },
    ) {
        HomeNavigationRailItems(
            displayDeveloperOptions = displayDeveloperOptions,
            selectedItem = selectedItem,
            onItemSelected = { item ->
                onItemSelected(item)
                scope.launch { state.collapse() }
            },
        )
    }
}

@Composable
private fun homeNavigationRailColors(): WideNavigationRailColors =
    WideNavigationRailDefaults
        .colors()
        .copy(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            modalContainerColor = MaterialTheme.colorScheme.surfaceBright,
        )

/** The menu items, with a divider after the last item of each group. */
@Composable
private fun HomeNavigationRailItems(
    displayDeveloperOptions: Boolean,
    selectedItem: HomeNavigationReelItem?,
    onItemSelected: (HomeNavigationReelItem) -> Unit,
) {
    val items =
        HomeNavigationReelItem.list(
            displayDeveloperOptions = displayDeveloperOptions,
        )
    items.forEachIndexed { index, item ->
        val selected = item == selectedItem
        val isLastOfGroup = items.getOrNull(index + 1)?.let { it.group != item.group } ?: false
        Column {
            WideNavigationRailItem(
                // The rail is only visible expanded. Reading its state here would switch the items
                // to the collapsed layout as soon as the rail starts closing, deforming them while
                // it slides out.
                railExpanded = true,
                selected = selected,
                onClick = { onItemSelected(item) },
                colors =
                    WideNavigationRailItemDefaults.colors().copy(
                        selectedIndicatorColor = MaterialTheme.colorScheme.surfaceContainer,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        selectedIconColor = TextColor.OnSurfaceLight,
                        unselectedIconColor = Outline.Dark,
                    ),
                icon = {
                    Icon(
                        imageVector = vectorResource(item.icon),
                        contentDescription = stringResource(item.label),
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.label),
                        modifier = Modifier.fillExpandedRailItemWidth(),
                    )
                },
            )
            if (isLastOfGroup) {
                HorizontalDivider(
                    modifier =
                        Modifier.padding(
                            horizontal = ExpandedRailItemHorizontalPadding,
                            vertical = Spacing.Spacing8,
                        ),
                )
            }
        }
    }
}

@Composable
private fun HomeNavigationReelHeader(
    serverName: String,
    userName: String,
) {
    Column(
        Modifier
            .extendToRailTop()
            .background(MaterialTheme.colorScheme.primary)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Image(
            vectorResource(Res.drawable.ic_dhis_logo),
            contentDescription = "",
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.size(Spacing.Spacing12))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = serverName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(Spacing.Spacing8))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = userName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Material3's WideNavigationRail adds a fixed, non-configurable top padding
 * (`NavigationRailCollapsedTokens.TopSpace`, 44.dp) above its header. This moves the header up
 * by that amount, so its background reaches the top of the rail, and shortens its reported
 * height by the same amount, so the items below it do not move.
 */
private val WideNavigationRailTopPadding = 44.dp

/** Horizontal padding of a WideNavigationRailItem (Material3 1.4.0 `ItemHorizontalPadding`). */
private val ExpandedRailItemHorizontalPadding = 20.dp

/**
 * Width that an expanded WideNavigationRailItem adds around its label, from the Material3 1.4.0
 * values: the item padding on both sides of the indicator (`ItemHorizontalPadding`, 20.dp), the
 * indicator padding on both sides (`FullWidthLeadingSpace`, 16.dp), the icon (24.dp) and the
 * space between icon and label (`IconLabelSpace`, 8.dp).
 */
private val ExpandedRailItemLabelOverhead =
    ExpandedRailItemHorizontalPadding * 2 + 16.dp * 2 + 24.dp + 8.dp

/**
 * The active indicator of an expanded WideNavigationRailItem is as wide as its icon and label.
 * Making the label take the rest of the rail width makes the indicator span the whole rail,
 * keeping the item's horizontal padding on both sides.
 */
private fun Modifier.fillExpandedRailItemWidth(): Modifier =
    layout { measurable, constraints ->
        val width =
            (constraints.maxWidth - ExpandedRailItemLabelOverhead.roundToPx())
                .coerceAtLeast(constraints.minWidth)
        val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
        layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
    }

private fun Modifier.extendToRailTop(): Modifier =
    layout { measurable, constraints ->
        val topPadding = WideNavigationRailTopPadding.roundToPx()
        val placeable = measurable.measure(constraints)
        layout(placeable.width, (placeable.height - topPadding).coerceAtLeast(0)) {
            placeable.placeRelative(0, -topPadding)
        }
    }

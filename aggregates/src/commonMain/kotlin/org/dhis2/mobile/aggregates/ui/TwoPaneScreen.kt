package org.dhis2.mobile.aggregates.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Two pane screen
 * Divides the screen in two parts for extended device screens
 * @param modifier: Modifier for styling
 * @param primaryPaneWeight: Weight of the primary pane
 * @param primaryPane: Primary pane content
 * @param secondaryPane: Secondary pane content
 * */
@Composable
fun TwoPaneScreen(
    modifier: Modifier = Modifier,
    primaryPaneWeight: Float,
    primaryPane: @Composable () -> Unit,
    secondaryPane: @Composable () -> Unit,
) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f - primaryPaneWeight),
        ) {
            secondaryPane()
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(primaryPaneWeight),
        ) {
            primaryPane()
        }
    }
}

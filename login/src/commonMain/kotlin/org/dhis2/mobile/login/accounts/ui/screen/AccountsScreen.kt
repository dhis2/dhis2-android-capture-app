package org.dhis2.mobile.login.accounts.ui.screen

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.login.accounts.ui.viewmodel.AccountsViewModel
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.add_account
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountsScreen() {
    val viewModel = koinViewModel<AccountsViewModel>()
    val accounts by viewModel.accounts.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        verticalArrangement = spacedBy(16.dp),
    ) {
        val scrollState = rememberLazyListState()
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .drawFadingEdgesBasic(scrollState),
            state = scrollState,
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = spacedBy(8.dp),
        ) {
            items(items = accounts) { account ->
                AccountItem(
                    account = account,
                    onItemClicked = viewModel::onAccountClicked,
                )
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            style = ButtonStyle.FILLED,
            text = stringResource(Res.string.add_account),
            icon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color.White,
                )
            },
            onClick = viewModel::onAddAccountClicked,
        )
    }
}

fun Modifier.drawFadingEdgesBasic(
    scrollableState: ScrollableState,
    topEdgeHeight: Dp = 16.dp,
) = then(
    Modifier
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()

            val topEdgeHeightPx = topEdgeHeight.toPx()

            if (scrollableState.canScrollBackward && topEdgeHeightPx >= 1f) {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White),
                            startY = 0f,
                            endY = topEdgeHeightPx,
                        ),
                    blendMode = BlendMode.DstIn,
                )
            }

            if (scrollableState.canScrollForward && topEdgeHeightPx >= 1f) {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(Color.White, Color.Transparent),
                            startY = size.height - topEdgeHeightPx,
                            endY = size.height,
                        ),
                    blendMode = BlendMode.DstIn,
                )
            }
        },
)

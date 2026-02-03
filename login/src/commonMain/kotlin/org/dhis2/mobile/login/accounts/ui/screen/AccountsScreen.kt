package org.dhis2.mobile.login.accounts.ui.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import org.dhis2.mobile.login.accounts.ui.viewmodel.AccountsViewModel
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.add_account
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AccountsScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val context = LocalPlatformContext.current
    val viewModel = koinViewModel<AccountsViewModel> { parametersOf(context) }
    val accounts by viewModel.accounts.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        verticalArrangement = spacedBy(16.dp),
    ) {
        val scrollState = rememberLazyListState()
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                Modifier
                    .fillMaxWidth(),
                state = scrollState,
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = spacedBy(8.dp),
            ) {
                items(items = accounts) { account ->
                    with(sharedTransitionScope) {
                        AccountItem(
                            modifier =
                                Modifier
                                    .sharedElement(
                                        sharedContentState = rememberSharedContentState(account.key()),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                    ),
                            account = account,
                            onItemClicked = viewModel::onAccountClicked,
                        )
                    }
                }
            }

            if (scrollState.canScrollBackward) {
                FadingEdge(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(16.dp),
                    colors = listOf(Color.White, Color.Transparent),
                )
            }
            if (scrollState.canScrollForward) {
                FadingEdge(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(16.dp),
                    colors = listOf(Color.Transparent, Color.White),
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

@Composable
fun FadingEdge(
    modifier: Modifier,
    colors: List<Color>,
) {
    Box(
        modifier =
            modifier.background(
                brush = Brush.verticalGradient(colors),
            ),
    )
}

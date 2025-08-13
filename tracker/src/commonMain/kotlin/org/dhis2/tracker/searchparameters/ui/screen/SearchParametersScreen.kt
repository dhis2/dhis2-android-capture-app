package org.dhis2.tracker.searchparameters.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.commons.extensions.deviceIsInLandscapeMode
import org.dhis2.mobile.commons.input.UiActionHandler
import org.dhis2.mobile.tracker.resources.Res
import org.dhis2.mobile.tracker.resources.clear_search
import org.dhis2.mobile.tracker.resources.empty_search_attributes_message
import org.dhis2.mobile.tracker.resources.search
import org.dhis2.tracker.searchparameters.ui.provider.provideParameterSelectorItem
import org.dhis2.tracker.searchparameters.ui.viewmodel.SearchParametersViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.ParameterSelectorItem
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SearchParametersScreen(
    uiActionHandler: UiActionHandler,
    onSearch: () -> Unit,
    onClear: () -> Unit,
) {
    val viewModel: SearchParametersViewModel =
        koinViewModel<SearchParametersViewModel>(parameters = {
            parametersOf(
                uiActionHandler,
            )
        })

    val snackBarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState) {
        uiState.minAttributesMessage?.let { message ->
            snackBarHostState.showSnackbar(message)
        }
    }

    val backgroundShape = when {
        deviceIsInLandscapeMode() -> RoundedCornerShape(
            topStart = CornerSize(Radius.L),
            topEnd = CornerSize(Radius.NoRounding),
            bottomEnd = CornerSize(Radius.NoRounding),
            bottomStart = CornerSize(Radius.NoRounding),
        )

        else -> Shape.LargeTop
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.padding(
                    start = 8.dp,
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 48.dp,
                ),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White, shape = backgroundShape)
                .padding(it),
        ) {
            Column(
                modifier = Modifier
                    .weight(1F)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (uiState.items.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        InfoBar(
                            modifier = Modifier.testTag("EMPTY_SEARCH_ATTRIBUTES_TEXT_TAG"),
                            text = stringResource(Res.string.empty_search_attributes_message),
                            textColor = AdditionalInfoItemColor.WARNING.color,
                            backgroundColor = AdditionalInfoItemColor.WARNING.color.copy(alpha = 0.1f),
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = "warning",
                                    tint = AdditionalInfoItemColor.WARNING.color,
                                )
                            },
                        )
                    }
                } else {
                    uiState.items.forEachIndexed { index, inputUiState ->
                        ParameterSelectorItem(
                            modifier = Modifier
                                .testTag("SEARCH_PARAM_ITEM"),
                            model = provideParameterSelectorItem(
                                inputUiState = inputUiState,
                                onAction = viewModel::onUiAction,
                            ),
                        )
                    }
                }

                if (uiState.clearSearchEnabled) {
                    Button(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp, 24.dp, 16.dp, 8.dp),
                        style = ButtonStyle.TEXT,
                        text = stringResource(Res.string.clear_search),
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Cancel,
                                contentDescription = stringResource(Res.string.clear_search),
                                tint = SurfaceColor.Primary,
                            )
                        },
                    ) {
                        focusManager.clearFocus()
                        onClear()
                    }
                }
            }

            Button(
                enabled = uiState.searchEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 16.dp, 8.dp)
                    .testTag("SEARCH_BUTTON"),
                style = ButtonStyle.FILLED,
                text = stringResource(Res.string.search),
                icon = {
                    val iconTint = if (uiState.searchEnabled) {
                        TextColor.OnPrimary
                    } else {
                        TextColor.OnDisabledSurface
                    }

                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(Res.string.search),
                        tint = iconTint,
                    )
                },
            ) {
                focusManager.clearFocus()
                onSearch()
            }
        }
    }
}

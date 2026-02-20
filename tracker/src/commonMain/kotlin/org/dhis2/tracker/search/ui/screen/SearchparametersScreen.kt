package org.dhis2.tracker.search.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.dhis2.mobile.tracker.resources.Res
import org.dhis2.mobile.tracker.resources.clear_search
import org.dhis2.mobile.tracker.resources.empty_search_attributes_message
import org.dhis2.mobile.tracker.resources.optional
import org.dhis2.mobile.tracker.resources.search
import org.dhis2.tracker.input.ui.action.TrackerInputUiEvent
import org.dhis2.tracker.input.ui.state.TrackerOptionItem
import org.dhis2.tracker.input.ui.state.loadOptionSetConfiguration
import org.dhis2.tracker.search.ui.action.SearchScreenUiEvent
import org.dhis2.tracker.search.ui.provider.provideParameterSelectorItem
import org.dhis2.tracker.search.ui.state.SearchParametersUiState
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

@Composable
fun SearchParametersScreen(
    uiState: SearchParametersUiState,
    onSearchScreenUiEvent: (SearchScreenUiEvent) -> Unit,
    onTrackerInputUiEvent: (TrackerInputUiEvent) -> Unit,
    isLandscape: Boolean,
    getOptionSetFlow: (fieldUid: String, optionSetUid: String) -> Flow<PagingData<TrackerOptionItem>>?,
    onOptionSetSearch: (fieldUid: String, query: String) -> Unit,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    uiState.minAttributesMessage?.let { message ->
        coroutineScope.launch {
            uiState.shouldShowMinAttributeWarning.collectLatest {
                if (it) {
                    snackBarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short,
                    )
                    uiState.updateMinAttributeWarning(false)
                }
            }
        }
    }

    LaunchedEffect(uiState.isOnBackPressed) {
        uiState.isOnBackPressed.collectLatest {
            if (it) {
                focusManager.clearFocus()
                onSearchScreenUiEvent(SearchScreenUiEvent.OnCloseClicked)
            }
        }
    }

    val backgroundShape =
        if (isLandscape) {
            RoundedCornerShape(
                topStart = CornerSize(Radius.L),
                topEnd = CornerSize(Radius.NoRounding),
                bottomEnd = CornerSize(Radius.NoRounding),
                bottomStart = CornerSize(Radius.NoRounding),
            )
        } else {
            Shape.LargeTop
        }

    Scaffold(
        containerColor = Color.Companion.Transparent,
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier =
                    Modifier.Companion.padding(
                        start = 8.dp,
                        top = 8.dp,
                        end = 8.dp,
                        bottom = 48.dp,
                    ),
            )
        },
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        Column(
            modifier =
                Modifier.Companion
                    .fillMaxSize()
                    .background(color = Color.Companion.White, shape = backgroundShape)
                    .padding(
                        top = 0.dp,
                        bottom = paddingValues.calculateBottomPadding(),
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        end = paddingValues.calculateEndPadding(layoutDirection),
                    ),
        ) {
            LazyColumn(
                modifier =
                    Modifier.Companion
                        .weight(1F),
            ) {
                if (uiState.items.isEmpty()) {
                    item {
                        Box(
                            modifier =
                                Modifier.Companion
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            contentAlignment = Alignment.Companion.Center,
                        ) {
                            InfoBar(
                                modifier = Modifier.Companion.testTag("EMPTY_SEARCH_ATTRIBUTES_TEXT_TAG"),
                                text = stringResource(Res.string.empty_search_attributes_message),
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = "warning",
                                        tint = AdditionalInfoItemColor.WARNING.color,
                                    )
                                },
                                textColor = AdditionalInfoItemColor.WARNING.color,
                                backgroundColor = AdditionalInfoItemColor.WARNING.color.copy(alpha = 0.1f),
                            )
                        }
                    }
                } else {
                    itemsIndexed(
                        items = uiState.items,
                        key = { _, fieldUiModel ->
                            fieldUiModel.uid
                        },
                    ) { index, trackerInputModel ->
                        ParameterSelectorItem(
                            modifier =
                                Modifier.Companion
                                    .testTag("SEARCH_PARAM_ITEM"),
                            model =
                                provideParameterSelectorItem(
                                    inputModel =
                                        trackerInputModel.loadOptionSetConfiguration(
                                            getOptionSetFlow = getOptionSetFlow,
                                            onOptionSetSearch = onOptionSetSearch,
                                        ),
                                    helperText = stringResource(Res.string.optional),
                                    onNextClicked = {
                                        val nextIndex = index + 1
                                        if (nextIndex < uiState.items.size) {
                                            onTrackerInputUiEvent(
                                                TrackerInputUiEvent.OnItemClick(
                                                    uiState.items[nextIndex].uid,
                                                ),
                                            )
                                        }
                                    },
                                    onUiEvent = onTrackerInputUiEvent,
                                ),
                        )
                    }
                }

                if (uiState.clearSearchEnabled) {
                    item {
                        Box(
                            modifier = Modifier.Companion.fillMaxWidth(),
                            contentAlignment = Alignment.Companion.Center,
                        ) {
                            Button(
                                modifier = Modifier.Companion.padding(16.dp, 24.dp, 16.dp, 8.dp),
                                style = ButtonStyle.TEXT,
                                text = stringResource(Res.string.clear_search),
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Cancel,
                                        contentDescription = "clear search",
                                        tint = SurfaceColor.Primary,
                                    )
                                },
                            ) {
                                focusManager.clearFocus()
                                onSearchScreenUiEvent(SearchScreenUiEvent.OnClearSearchButtonClicked)
                            }
                        }
                    }
                }
            }

            Button(
                enabled = uiState.searchEnabled,
                modifier =
                    Modifier.Companion
                        .fillMaxWidth()
                        .padding(16.dp, 8.dp, 16.dp, 8.dp)
                        .testTag("SEARCH_BUTTON"),
                style = ButtonStyle.FILLED,
                text = stringResource(Res.string.search),
                icon = {
                    val iconTint =
                        if (uiState.searchEnabled) {
                            TextColor.OnPrimary
                        } else {
                            TextColor.OnDisabledSurface
                        }
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "search",
                        tint = iconTint,
                    )
                },
            ) {
                focusManager.clearFocus()
                onSearchScreenUiEvent(SearchScreenUiEvent.OnSearchButtonClicked)
            }
        }
    }
}

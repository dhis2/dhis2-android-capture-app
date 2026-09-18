package org.dhis2.usescases.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.dhis2.R
import org.dhis2.usescases.settings.models.SyncErrorLogUiState
import org.dhis2.usescases.settings.ui.actions.ShareDataChooser
import org.dhis2.usescases.settings.ui.viewmodels.SyncErrorLogViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.SelectionState
import org.hisp.dhis.mobile.ui.designsystem.component.TopBar
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SyncErrorLogDialog(onDismiss: () -> Unit) {
    val viewModel = koinViewModel<SyncErrorLogViewModel>()
    val uiState by viewModel.errorLogUiState.collectAsState()
    val shareEvent by viewModel.shareEvent.collectAsState(null)

    shareEvent?.let { data ->
        ShareDataChooser(data)
    }

    Dialog(
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false,
                decorFitsSystemWindows = false,
            ),
        onDismissRequest = onDismiss,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBar(
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    navigationIcon = {
                        when (uiState) {
                            SyncErrorLogUiState.Loading,
                            is SyncErrorLogUiState.Log,
                            -> {
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.testTag("ERROR_LOG_BACK_BUTTON"),
                                    icon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                            contentDescription = "Back Button",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    },
                                )
                            }

                            is SyncErrorLogUiState.Selection -> {
                                IconButton(
                                    onClick = viewModel::exitSelectionMode,
                                    modifier = Modifier.testTag("ERROR_LOG_CLEAR_SELECTION_BUTTON"),
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Clear,
                                            contentDescription = "clear Button",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    },
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = "Sync Error Log",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    },
                    actions = {
                        when (uiState) {
                            SyncErrorLogUiState.Loading,
                            is SyncErrorLogUiState.Selection,
                            -> {
                                // Nothing to do here
                            }

                            is SyncErrorLogUiState.Log -> {
                                IconButton(
                                    onClick = viewModel::initSelectionMode,
                                    modifier = Modifier.testTag("ERROR_LOG_SHARE_BUTTON"),
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Share,
                                            contentDescription = "Share Button",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    },
                                )
                            }
                        }
                    },
                )
            },
            content = { padding ->
                Box(
                    Modifier
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.primary)
                        .background(
                            color = Color.White,
                            shape =
                                RoundedCornerShape(
                                    topStart = Spacing.Spacing16,
                                    topEnd = Spacing.Spacing16,
                                ),
                        ),
                ) {
                    LazyColumn(
                        Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(bottom = Spacing.Spacing104),
                    ) {
                        itemsIndexed(
                            items =
                                when (uiState) {
                                    SyncErrorLogUiState.Loading -> emptyList()
                                    is SyncErrorLogUiState.Log -> (uiState as SyncErrorLogUiState.Log).errorList
                                    is SyncErrorLogUiState.Selection -> (uiState as SyncErrorLogUiState.Selection).errorList
                                },
                            key = { _, errorItem -> errorItem.hashCode() },
                        ) { index, errorItem ->
                            ListCard(
                                modifier = Modifier.fillMaxWidth(),
                                listCardState =
                                    rememberListCardState(
                                        title =
                                            ListCardTitleModel(
                                                text = errorItem.errorCode ?: "",
                                            ),
                                        description =
                                            ListCardDescriptionModel(
                                                text = errorItem.errorComponent ?: "",
                                            ),
                                        lastUpdated = errorItem.creationDateLabel,
                                        additionalInfoColumnState =
                                            rememberAdditionalInfoColumnState(
                                                additionalInfoList =
                                                    listOf(
                                                        AdditionalInfoItem(
                                                            value = errorItem.errorDescription ?: "",
                                                        ),
                                                    ),
                                                syncProgressItem =
                                                    AdditionalInfoItem(
                                                        key = "",
                                                        value = "",
                                                    ),
                                            ),
                                        selectionState =
                                            when (uiState) {
                                                SyncErrorLogUiState.Loading,
                                                is SyncErrorLogUiState.Log,
                                                -> SelectionState.NONE

                                                is SyncErrorLogUiState.Selection -> {
                                                    if (errorItem.isSelected) {
                                                        SelectionState.SELECTED
                                                    } else {
                                                        SelectionState.SELECTABLE
                                                    }
                                                }
                                            },
                                    ),
                                onCardClick = {},
                                onCardSelected = { _ ->
                                    viewModel.setSelected(index)
                                },
                            )
                        }
                    }

                    AnimatedVisibility(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        visible = uiState is SyncErrorLogUiState.Selection,
                        enter = scaleIn(),
                        exit = scaleOut(),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .padding(
                                        start = Spacing.Spacing16,
                                        end = Spacing.Spacing16,
                                        bottom = Spacing.Spacing4,
                                    ),
                        ) {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                style = ButtonStyle.FILLED,
                                text = stringResource(R.string.share),
                                onClick = viewModel::onShareLog,
                            )
                        }
                    }
                }
            },
        )
    }
}

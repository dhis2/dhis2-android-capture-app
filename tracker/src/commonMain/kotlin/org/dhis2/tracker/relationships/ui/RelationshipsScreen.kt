package org.dhis2.tracker.relationships.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.commons.model.AvatarProviderConfiguration
import org.dhis2.mobile.tracker.resources.Res
import org.dhis2.mobile.tracker.resources.cancel
import org.dhis2.mobile.tracker.resources.empty_relationships
import org.dhis2.mobile.tracker.resources.no_data
import org.dhis2.mobile.tracker.resources.no_relationships
import org.dhis2.mobile.tracker.resources.remove
import org.dhis2.mobile.tracker.resources.remove_relationship_desc
import org.dhis2.mobile.tracker.resources.remove_relationship_title
import org.dhis2.mobile.tracker.resources.remove_some_relationships_desc
import org.dhis2.mobile.tracker.resources.remove_some_relationships_title
import org.dhis2.mobile.tracker.resources.show_less
import org.dhis2.mobile.tracker.resources.show_more
import org.dhis2.mobile.tracker.resources.show_number_more
import org.dhis2.mobile.tracker.resources.syncing
import org.dhis2.tracker.relationships.ui.state.ListSelectionState
import org.dhis2.tracker.relationships.ui.state.RelationshipItemUiState
import org.dhis2.tracker.relationships.ui.state.RelationshipSectionUiState
import org.dhis2.tracker.relationships.ui.state.RelationshipsUiState
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyleData
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonBlock
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ColorStyle
import org.hisp.dhis.mobile.ui.designsystem.component.Description
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.component.Title
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellDefaults
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.hisp.dhis.mobile.ui.designsystem.files.buildPainterForFile
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2TextStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow
import org.hisp.dhis.mobile.ui.designsystem.theme.getTextStyle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun RelationShipsScreen(
    uiState: RelationshipsUiState,
    relationshipSelectionState: ListSelectionState,
    onCreateRelationshipClick: (RelationshipSectionUiState) -> Unit,
    onRelationshipClick: (RelationshipItemUiState) -> Unit,
    onRelationShipSelected: (String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long,
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.dropShadow(shape = SnackbarDefaults.shape),
                    snackbarData = data,
                    containerColor = SurfaceColor.SurfaceBright,
                    contentColor = TextColor.OnSurface,
                )
            }
        },
    ) { contentPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(color = Color.White, shape = Shape.LargeTop)
                    .padding(),
            verticalArrangement = spacedBy(Spacing.Spacing4),
            contentPadding = contentPadding,
        ) {
            when (uiState) {
                is RelationshipsUiState.Loading -> {
                    item {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            ProgressIndicator(type = ProgressIndicatorType.CIRCULAR)
                        }
                    }
                }

                is RelationshipsUiState.Empty,
                is RelationshipsUiState.Error,
                -> {
                    item { NoRelationships() }
                }

                is RelationshipsUiState.Success -> {
                    items(uiState.data) { item ->
                        RelationShipTypeSection(
                            title = item.title,
                            description =
                                if (item.relationships.isEmpty()) {
                                    stringResource(resource = Res.string.no_data)
                                } else {
                                    null
                                },
                            relationships = item.relationships,
                            canAddRelationship = item.entityToAdd != null,
                            relationshipSelectionState = relationshipSelectionState,
                            onCreateRelationshipClick = {
                                onCreateRelationshipClick(item)
                            },
                            onRelationshipClick = {
                                onRelationshipClick(it)
                            },
                            onRelationshipSelected = onRelationShipSelected,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RelationShipTypeSection(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    relationships: List<RelationshipItemUiState>,
    canAddRelationship: Boolean,
    relationshipSelectionState: ListSelectionState,
    onCreateRelationshipClick: () -> Unit,
    onRelationshipClick: (RelationshipItemUiState) -> Unit,
    onRelationshipSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val displayedItems = if (expanded) relationships else relationships.take(3)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(Spacing.Spacing12),
        verticalArrangement = spacedBy(Spacing.Spacing4),
    ) {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = Spacing.Spacing48)
                    .wrapContentHeight(),
            horizontalArrangement = spacedBy(Spacing.Spacing16),
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .align(Alignment.CenterVertically),
            ) {
                Title(text = title)

                description?.let {
                    Description(
                        text = it,
                        textColor = TextColor.OnSurfaceLight,
                    )
                }
            }
            if (canAddRelationship) {
                IconButton(
                    modifier = Modifier.testTag(TEST_ADD_RELATIONSHIP_BUTTON),
                    style = IconButtonStyle.FILLED,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "New event",
                            tint = Color.White,
                        )
                    },
                    onClick = onCreateRelationshipClick,
                )
            }
        }

        displayedItems.forEach { item ->
            ListCard(
                listCardState =
                    rememberListCardState(
                        title = ListCardTitleModel(text = item.title),
                        description = item.description?.let { ListCardDescriptionModel(text = it) },
                        lastUpdated = item.lastUpdated,
                        selectionState =
                            relationshipSelectionState.isSelected(
                                item.uid,
                                item.canOpen,
                            ),
                        additionalInfoColumnState =
                            rememberAdditionalInfoColumnState(
                                additionalInfoList =
                                    item.attributes.map {
                                        AdditionalInfoItem(
                                            key = it.first,
                                            value = it.second,
                                        )
                                    },
                                syncProgressItem =
                                    AdditionalInfoItem(
                                        key = stringResource(resource = Res.string.syncing),
                                        value = "",
                                    ),
                                minItemsToShow = 3,
                                expandLabelText = stringResource(resource = Res.string.show_more),
                                shrinkLabelText = stringResource(resource = Res.string.show_less),
                                scrollableContent = false,
                            ),
                    ),
                listAvatar = {
                    Avatar(
                        style =
                            when (
                                val config = item.avatar
                            ) {
                                is AvatarProviderConfiguration.MainValueLabel ->
                                    AvatarStyleData.Text(
                                        config.firstMainValue
                                            .firstOrNull()
                                            ?.toString()
                                            ?: "?",
                                    )

                                is AvatarProviderConfiguration.Metadata ->
                                    AvatarStyleData.Metadata(
                                        imageCardData = config.metadataIconData.imageCardData,
                                        avatarSize = config.size,
                                        tintColor = config.metadataIconData.color,
                                    )

                                is AvatarProviderConfiguration.ProfilePic ->
                                    AvatarStyleData.Image(buildPainterForFile(config.profilePicturePath))
                            },
                        onImageClick = {},
                    )
                },
                onCardClick = { if (item.canOpen) onRelationshipClick(item) },
                onCardSelected = {
                    if (item.canOpen) {
                        onRelationshipSelected(item.uid)
                    }
                },
            )
        }

        if (relationships.size > 3) {
            val showMoreText =
                stringResource(resource = Res.string.show_number_more, relationships.size - 3)
            val showLessText = stringResource(resource = Res.string.show_less) + "..."
            Button(
                style = ButtonStyle.TEXT,
                text =
                    if (expanded) {
                        showLessText
                    } else {
                        showMoreText
                    },
            ) {
                expanded = !expanded
            }
        }
    }
}

@Composable
internal fun NoRelationships() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(Spacing.Spacing48),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            modifier =
                Modifier
                    .padding(Spacing.Spacing1)
                    .fillMaxWidth(),
            painter = rememberVectorPainter(vectorResource(resource = Res.drawable.no_relationships)),
            contentDescription = stringResource(resource = Res.string.empty_relationships),
        )
        Spacer(
            modifier =
                Modifier
                    .height(Spacing.Spacing16)
                    .fillMaxWidth(),
        )
        Text(
            text = stringResource(resource = Res.string.empty_relationships),
            style = getTextStyle(style = DHIS2TextStyle.BODY_MEDIUM),
        )
    }
}

@Composable
fun DeleteRelationshipsConfirmation(
    relationships: List<String>,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    BottomSheetShell(
        uiState =
            BottomSheetShellUIState(
                headerTextAlignment = TextAlign.Start,
                showTopSectionDivider = true,
                title =
                    when (relationships.size) {
                        1 -> stringResource(Res.string.remove_relationship_title, relationships[0])
                        else ->
                            stringResource(
                                Res.string.remove_some_relationships_title,
                                relationships.size,
                            )
                    },
                description =
                    when (relationships.size) {
                        1 -> stringResource(Res.string.remove_relationship_desc, relationships[0])
                        else ->
                            stringResource(
                                Res.string.remove_some_relationships_desc,
                                relationships.size,
                            )
                    },
            ),
        icon = {
            Icon(
                Icons.Outlined.ErrorOutline,
                tint = TextColor.OnErrorContainer,
                contentDescription = "error",
            )
        },
        buttonBlock = {
            ButtonBlock(
                modifier = Modifier.padding(BottomSheetShellDefaults.buttonBlockPaddings()),
                primaryButton = {
                    Button(
                        style = ButtonStyle.OUTLINED,
                        text = stringResource(Res.string.cancel),
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                secondaryButton = {
                    Button(
                        style = ButtonStyle.FILLED,
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                            )
                        },
                        text = stringResource(Res.string.remove),
                        colorStyle = ColorStyle.ERROR,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                    )
                },
            )
        },
        onDismiss = onDismiss,
        content = null,
    )
}

const val TEST_ADD_RELATIONSHIP_BUTTON = "TEST_ADD_RELATIONSHIP_BUTTON"

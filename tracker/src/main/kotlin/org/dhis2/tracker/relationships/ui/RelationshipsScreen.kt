package org.dhis2.tracker.relationships.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.commons.data.RelationshipOwnerType
import org.dhis2.tracker.relationships.model.RelationShipItem
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.dhis2.ui.avatar.AvatarProvider
import org.dhis2.ui.avatar.AvatarProviderConfiguration
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Description
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.component.Title
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun RelationShipsScreen(
    viewModel: RelationShipsViewModel,
    onCreateRelationshipClick: (RelationshipSection) -> Unit,
    onRelationshipClick: (RelationShipItem) -> Unit,
) {
    val uiState by viewModel.relationshipsUiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 8.dp),
        verticalArrangement = spacedBy(Spacing.Spacing4),
    ) {
        when (val currentState = uiState) {
            is RelationshipsListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    ProgressIndicator(type = ProgressIndicatorType.CIRCULAR)
                }
            }

            is RelationshipsListUiState.Empty,
            is RelationshipsListUiState.Error -> NoRelationships()

            is RelationshipsListUiState.Success -> {
                LazyColumn {
                    items(currentState.data) { item ->
                        RelationShipTypeSection(
                            title = item.relationshipType.displayName() ?: "",
                            description = if (item.relationships.isEmpty()) "No data" else null,
                            relationships = item.relationships,
                            onCreateRelationshipClick = {
                                onCreateRelationshipClick(item)
                            },
                            onRelationshipClick = {
                                onRelationshipClick(it)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RelationShipTypeSection(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    relationships: List<RelationShipItem>,
    onCreateRelationshipClick: () -> Unit,
    onRelationshipClick: (RelationShipItem) -> Unit,
) {
    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp),
            horizontalArrangement = spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Title(text = title)

                description?.let {
                    Description(
                        text = it,
                        textColor = TextColor.OnSurfaceLight,
                    )
                }
            }
            IconButton(
                modifier = Modifier.testTag(TEST_ADD_RELATIONSHIP_BUTTON),
                style = IconButtonStyle.FILLED,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "New event",
                        tint = MaterialTheme.colors.onPrimary,
                    )
                },
                onClick = onCreateRelationshipClick,
            )
        }
        relationships.forEach { item ->
            ListCard(
                listCardState = rememberListCardState(
                    title = ListCardTitleModel(text = item.title),
                    additionalInfoColumnState = rememberAdditionalInfoColumnState(
                        additionalInfoList = item.attributes.map {
                            AdditionalInfoItem(
                                key = it.first,
                                value = it.second,
                            )
                        },
                        syncProgressItem = AdditionalInfoItem(
                            key = "null",
                            value = "null",
                        ),
                        expandLabelText = "null",
                        shrinkLabelText = "null",
                        minItemsToShow = 3,
                        scrollableContent = false,
                    ),
                ),
                listAvatar = {
                    AvatarProvider(
                        avatarProviderConfiguration = item.avatar,
                    ) { }
                },
                onCardClick = { if (item.canOpen) onRelationshipClick(item) }
            )
        }
    }
}

@Preview
@Composable
fun RelationShipTypeSectionPreview() {
    Box(
        modifier = Modifier
            .background(Color.White)
    ) {
        RelationShipTypeSection(
            title = "Relationship type",
            description = "No data",
            relationships = listOf(
                RelationShipItem(
                    title = "Relationship title",
                    attributes = emptyList(),
                    ownerType = RelationshipOwnerType.TEI,
                    ownerUid = "ownerUid",
                    avatar = AvatarProviderConfiguration.ProfilePic("", "P"),
                    canOpen = true,
                )
            ),
            onCreateRelationshipClick = {},
            onRelationshipClick = {},
        )
    }
}

const val TEST_ADD_RELATIONSHIP_BUTTON = "TEST_ADD_RELATIONSHIP_BUTTON"

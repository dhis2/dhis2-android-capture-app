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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.commons.data.RelationshipOwnerType
import org.dhis2.tracker.R
import org.dhis2.tracker.relationships.model.RelationShipItem
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.dhis2.ui.avatar.AvatarProvider
import org.dhis2.ui.avatar.AvatarProviderConfiguration
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Description
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.component.Title
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun RelationShipsScreen(
    uiState: RelationshipsListUiState<List<RelationshipSection>>,
    onCreateRelationshipClick: (RelationshipSection) -> Unit,
    onRelationshipClick: (RelationShipItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White, shape = Shape.LargeTop)
            .padding(horizontal = Spacing.Spacing8),
        verticalArrangement = spacedBy(Spacing.Spacing4),
    ) {
        when (uiState) {
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
                    items(uiState.data) { item ->
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        verticalArrangement = spacedBy(4.dp),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
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
                    description = item.description?.let { ListCardDescriptionModel(text = it) },
                    lastUpdated = item.lastUpdated,
                    additionalInfoColumnState = rememberAdditionalInfoColumnState(
                        additionalInfoList = item.attributes.map {
                            AdditionalInfoItem(
                                key = it.first,
                                value = it.second,
                            )
                        },
                        syncProgressItem = AdditionalInfoItem(
                            key = stringResource(id = R.string.syncing),
                            value = "",
                        ),
                        minItemsToShow = 3,
                        expandLabelText = stringResource(id = R.string.show_more),
                        shrinkLabelText = stringResource(id = R.string.show_less),
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
fun RelationShipScreenPreview() {
    val mockUiState = RelationshipsListUiState.Success(
        data = listOf(
            RelationshipSection(
                relationshipType = RelationshipType.builder()
                    .uid("")
                    .displayName("Relationship type")
                    .build(),
                relationships = listOf(
                    RelationShipItem(
                        title = "First name: Peter",
                        description = null,
                        attributes = listOf(
                            Pair("Last name", "Parker"),
                            Pair("Age", "25"),
                            Pair("Gender", "Male"),
                            Pair("Height", "1.75"),
                        ),
                        ownerType = RelationshipOwnerType.TEI,
                        ownerUid = "ownerUid",
                        avatar =  AvatarProviderConfiguration.MainValueLabel(
                            firstMainValue = "P",
                        ),
                        canOpen = true,
                        lastUpdated = "Yesterday",
                    ),
                    RelationShipItem(
                        title = "First name: Mario",
                        description = null,
                        attributes = listOf(
                            Pair("Last name", "Bros"),
                            Pair("Age", "25"),
                            Pair("Gender", "Male"),
                            Pair("Height", "1.75"),
                        ),
                        ownerType = RelationshipOwnerType.TEI,
                        ownerUid = "ownerUid",
                        avatar = AvatarProviderConfiguration.MainValueLabel(
                            firstMainValue = "M",
                        ),
                        canOpen = true,
                        lastUpdated = "Yesterday",
                    )
                ),
                teiTypeUid = "teiTypeUid",
            ),
            RelationshipSection(
                relationshipType = RelationshipType.builder()
                    .uid("")
                    .displayName("Empty relation ship")
                    .build(),
                relationships = emptyList(),
                teiTypeUid = "teiTypeUid",

                )
        )
    )

    RelationShipsScreen(
        uiState = mockUiState,
        onCreateRelationshipClick = {},
        onRelationshipClick = {}
    )
}

const val TEST_ADD_RELATIONSHIP_BUTTON = "TEST_ADD_RELATIONSHIP_BUTTON"

package org.dhis2.tracker.relationships.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.tracker.R
import org.dhis2.tracker.relationships.model.RelationshipItem
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
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
    uiState: RelationshipsUiState<List<RelationshipSection>>,
    onCreateRelationshipClick: (RelationshipSection) -> Unit,
    onRelationshipClick: (RelationshipItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White, shape = Shape.LargeTop)
            .padding(horizontal = Spacing.Spacing8),
        verticalArrangement = spacedBy(Spacing.Spacing4),
    ) {
        when (uiState) {
            is RelationshipsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    ProgressIndicator(type = ProgressIndicatorType.CIRCULAR)
                }
            }

            is RelationshipsUiState.Empty,
            is RelationshipsUiState.Error -> NoRelationships()

            is RelationshipsUiState.Success -> {
                LazyColumn {
                    items(uiState.data) { item ->
                        RelationShipTypeSection(
                            title = item.relationshipType.displayName() ?: "",
                            description = if (item.relationships.isEmpty()) "No data" else null,
                            relationships = item.relationships,
                            canAddRelationship = item.canAddRelationship(),
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
private fun RelationShipTypeSection(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    relationships: List<RelationshipItem>,
    canAddRelationship: Boolean,
    onCreateRelationshipClick: () -> Unit,
    onRelationshipClick: (RelationshipItem) -> Unit,
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
            if(canAddRelationship) {
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

@Composable
private fun NoRelationships() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            modifier = Modifier
                .padding(1.dp)
                .width(139.dp)
                .height(125.dp),
            painter = painterResource(id = R.drawable.no_relationships),
            contentDescription = stringResource(id = R.string.empty_relationships),
        )
        Spacer(
            modifier = Modifier
                .height(17.dp)
                .fillMaxWidth(),
        )
        Text(
            text = stringResource(id = R.string.empty_relationships),
            style = TextStyle(
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(id = R.color.gray_990),
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Preview
@Composable
fun NoRelationshipsPreview() {
    NoRelationships()
}

@Preview
@Composable
fun RelationShipScreenPreview() {
    val mockUiState = RelationshipsUiState.Success(
        data = listOf(
            RelationshipSection(
                relationshipType = RelationshipType.builder()
                    .uid("")
                    .displayName("Relationship type")
                    .build(),
                relationships = listOf(
                    RelationshipItem(
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
                    RelationshipItem(
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
                teiTypeUid = null,
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

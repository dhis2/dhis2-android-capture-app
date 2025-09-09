package org.dhis2.tracker.relationships.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.mobile.commons.model.AvatarProviderConfiguration
import org.dhis2.tracker.relationships.model.RelationshipConstraintSide
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.dhis2.tracker.relationships.ui.state.ListSelectionState
import org.dhis2.tracker.relationships.ui.state.RelationshipItemUiState
import org.dhis2.tracker.relationships.ui.state.RelationshipSectionUiState
import org.dhis2.tracker.relationships.ui.state.RelationshipsUiState

@Preview
@Composable
fun NoRelationshipsPreview() {
    NoRelationships()
}

@Preview
@Composable
fun RelationShipScreenPreview() {
    val mockUiState =
        RelationshipsUiState.Success(
            data =
                listOf(
                    RelationshipSectionUiState(
                        uid = "uid1",
                        title = "Relationship type",
                        relationships =
                            listOf(
                                RelationshipItemUiState(
                                    uid = "uidA",
                                    title = "First name: Peter",
                                    description = null,
                                    attributes =
                                        listOf(
                                            Pair("Last name", "Parker"),
                                            Pair("Age", "25"),
                                            Pair("Gender", "Male"),
                                            Pair("Height", "1.75"),
                                        ),
                                    ownerType = RelationshipOwnerType.TEI,
                                    ownerUid = "ownerUid",
                                    avatar =
                                        AvatarProviderConfiguration.MainValueLabel(
                                            firstMainValue = "P",
                                        ),
                                    canOpen = true,
                                    lastUpdated = "Yesterday",
                                ),
                                RelationshipItemUiState(
                                    uid = "uidB",
                                    title = "First name: Mario",
                                    description = null,
                                    attributes =
                                        listOf(
                                            Pair("Last name", "Bros"),
                                            Pair("Age", "25"),
                                            Pair("Gender", "Male"),
                                            Pair("Height", "1.75"),
                                        ),
                                    ownerType = RelationshipOwnerType.TEI,
                                    ownerUid = "ownerUid",
                                    avatar =
                                        AvatarProviderConfiguration.MainValueLabel(
                                            firstMainValue = "M",
                                        ),
                                    canOpen = true,
                                    lastUpdated = "Yesterday",
                                ),
                            ),
                        side = RelationshipConstraintSide.FROM,
                        entityToAdd = null,
                    ),
                    RelationshipSectionUiState(
                        uid = "uid2",
                        title = "Empty relation ship",
                        relationships = emptyList(),
                        side = RelationshipConstraintSide.FROM,
                        entityToAdd = null,
                    ),
                ),
        )

    RelationShipsScreen(
        uiState = mockUiState,
        relationshipSelectionState = ListSelectionState(),
        onCreateRelationshipClick = {},
        onRelationshipClick = {},
        onRelationShipSelected = {},
    )
}

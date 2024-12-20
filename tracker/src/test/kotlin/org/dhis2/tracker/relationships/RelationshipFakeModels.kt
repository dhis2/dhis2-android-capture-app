package org.dhis2.tracker.relationships

import org.dhis2.tracker.relationships.model.RelationshipConstraintSide
import org.dhis2.tracker.relationships.model.RelationshipDirection
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipConstraint
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.mockito.kotlin.mock
import java.util.Date

val relationshipSection1 = RelationshipSection(
    uid = "relationshipTypeUid1",
    title = "RelationshipType1 FROM",
    relationships = emptyList(),
    side = RelationshipConstraintSide.FROM,
    entityToAdd = "trackedEntityType2",
)

val relationshipSection2 =
    RelationshipSection(
        uid = "relationshipTypeUid2",
        title = "RelationshipType2 FROM",
        relationships = emptyList(),
        side = RelationshipConstraintSide.FROM,
        entityToAdd = "trackedEntityType2",
    )

 val relationshipModel1 = RelationshipModel(
    ownerType = RelationshipOwnerType.TEI,
    ownerUid = "OwnerUid1",
    canBeOpened = true,
    fromValues = listOf(
        "MainValue1" to "Value1",
        "SecondMainValue1" to "SecondValue1",
    ),
    toValues = emptyList(),
    fromImage = null,
    toImage = null,
    fromDefaultImageResource = 0,
    toDefaultImageResource = 0,
    ownerStyle = mock(),
    fromLastUpdated = Date(),
    toLastUpdated = Date(),
    fromDescription = "Description 1",
    toDescription = "Description 1",
    fromGeometry = null,
    toGeometry = null,
    direction = RelationshipDirection.FROM,
    relationship = Relationship.builder().uid("uid1").build()
)

 val relationshipModel2 = RelationshipModel(
    ownerType = RelationshipOwnerType.TEI,
    ownerUid = "OwnerUid2",
    canBeOpened = true,
    fromValues = listOf(
        "MainValue2" to "Value2",
        "SecondMainValue2" to "SecondValue2",
    ),
    toValues = emptyList(),
    fromImage = null,
    toImage = null,
    fromDefaultImageResource = 0,
    toDefaultImageResource = 0,
    ownerStyle = mock(),
    fromLastUpdated = Date(),
    toLastUpdated = Date(),
    fromDescription = "Description 2",
    toDescription = "Description 2",
    fromGeometry = null,
    toGeometry = null,
    direction = RelationshipDirection.FROM,
    relationship = Relationship.builder().uid("uid2").build()
)

val relationshipTypeTeiToTei = RelationshipType.builder()
    .uid("relationshipTypeUid1")
    .fromToName("RelationshipType1 FROM")
    .toFromName("RelationshipType1 TO")
    .displayName("Tei to Tei relationship")
    .fromConstraint(
        RelationshipConstraint.builder()
            .trackedEntityType(
                ObjectWithUid.create("trackedEntityType1")
            ).build()
    )
    .toConstraint(
        RelationshipConstraint.builder()
            .trackedEntityType(
                ObjectWithUid.create("trackedEntityType2")
            )
            .build()
    )
    .build()

val relationshipTypeEventToTei = RelationshipType.builder()
    .uid("relationshipTypeUid2")
    .fromToName("RelationshipType2 FROM")
    .toFromName("RelationshipType2 TO")
    .displayName("Event to Tei relationship")
    .fromConstraint(
        RelationshipConstraint.builder()
            .programStage(
                ObjectWithUid.create("programStageUid")
            )
            .build()
    )
    .toConstraint(
        RelationshipConstraint.builder()
            .trackedEntityType(
                ObjectWithUid.create("trackedEntityType2")
            )
            .build()
    )
    .build()
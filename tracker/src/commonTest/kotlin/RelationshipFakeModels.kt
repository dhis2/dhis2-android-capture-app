package org.dhis2.tracker.relationships

import org.dhis2.tracker.relationships.model.RelationshipConstraintSide
import org.dhis2.tracker.relationships.model.RelationshipDirection
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.dhis2.tracker.relationships.model.RelationshipSection
import java.util.Date

val relationshipSection1 =
    RelationshipSection(
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

val relationshipModel1 =
    RelationshipModel(
        ownerType = RelationshipOwnerType.TEI,
        ownerUid = "OwnerUid1",
        canBeOpened = true,
        fromValues =
            listOf(
                "MainValue1" to "Value1",
                "SecondMainValue1" to "SecondValue1",
            ),
        toValues = emptyList(),
        fromImage = null,
        toImage = null,
        fromDefaultImageResource = 0,
        toDefaultImageResource = 0,
        ownerStyleColor = "#FFFFFF",
        ownerStyleIcon = "icon",
        fromLastUpdated = Date(),
        toLastUpdated = Date(),
        fromDescription = "Description 1",
        toDescription = "Description 1",
        fromGeometry = null,
        toGeometry = null,
        direction = RelationshipDirection.FROM,
        relationshipUid = "uid1",
        relationshipState = "SYNCED",
    )

val relationshipModel2 =
    RelationshipModel(
        ownerType = RelationshipOwnerType.TEI,
        ownerUid = "OwnerUid2",
        canBeOpened = true,
        fromValues =
            listOf(
                "MainValue2" to "Value2",
                "SecondMainValue2" to "SecondValue2",
            ),
        toValues = emptyList(),
        fromImage = null,
        toImage = null,
        fromDefaultImageResource = 0,
        toDefaultImageResource = 0,
        ownerStyleIcon = null,
        ownerStyleColor = null,
        fromLastUpdated = Date(),
        toLastUpdated = Date(),
        fromDescription = "Description 2",
        toDescription = "Description 2",
        fromGeometry = null,
        toGeometry = null,
        direction = RelationshipDirection.FROM,
        relationshipUid = "uid2",
        relationshipState = "SYNCED",
    )

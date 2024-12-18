package org.dhis2.tracker.relationships

import org.dhis2.tracker.relationships.model.RelationshipConstraintSide
import org.dhis2.tracker.relationships.model.RelationshipDirection
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.hisp.dhis.android.core.relationship.Relationship
import org.mockito.kotlin.mock
import java.util.Date

val relationshipSection1 = RelationshipSection(
    uid = "uid1",
    title = "Relationship title 1",
    relationships = emptyList(),
    side = RelationshipConstraintSide.FROM,
    entityToAdd = null,
)

val relationshipSection2 =
    RelationshipSection(
        uid = "uid2",
        title = "Relationship title 2",
        relationships = emptyList(),
        side = RelationshipConstraintSide.FROM,
        entityToAdd = null,
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
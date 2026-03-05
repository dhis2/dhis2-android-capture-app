package org.dhis2.tracker.relationships

import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.relationship.RelationshipConstraint
import org.hisp.dhis.android.core.relationship.RelationshipType

val relationshipTypeTeiToTei: RelationshipType =
    RelationshipType
        .builder()
        .uid("relationshipTypeUid1")
        .fromToName("RelationshipType1 FROM")
        .toFromName("RelationshipType1 TO")
        .displayName("Tei to Tei relationship")
        .fromConstraint(
            RelationshipConstraint
                .builder()
                .trackedEntityType(
                    ObjectWithUid.create("trackedEntityType1"),
                ).build(),
        ).toConstraint(
            RelationshipConstraint
                .builder()
                .trackedEntityType(
                    ObjectWithUid.create("trackedEntityType2"),
                ).build(),
        ).access(
            Access.builder().read(true).build(),
        ).build()

val relationshipTypeEventToTei: RelationshipType =
    RelationshipType
        .builder()
        .uid("relationshipTypeUid2")
        .fromToName("RelationshipType2 FROM")
        .toFromName("RelationshipType2 TO")
        .displayName("Event to Tei relationship")
        .fromConstraint(
            RelationshipConstraint
                .builder()
                .programStage(
                    ObjectWithUid.create("programStageUid"),
                ).build(),
        ).toConstraint(
            RelationshipConstraint
                .builder()
                .trackedEntityType(
                    ObjectWithUid.create("trackedEntityType2"),
                ).build(),
        ).access(
            Access.builder().read(true).build(),
        ).build()

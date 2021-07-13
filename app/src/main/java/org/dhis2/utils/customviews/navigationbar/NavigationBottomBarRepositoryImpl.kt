package org.dhis2.utils.customviews.navigationbar

import org.hisp.dhis.android.core.D2

class NavigationBottomBarRepositoryImpl(private val d2: D2) : NavigationBottomBarRepository {

    override fun getRelationshipTypeCount(teiUid: String): Int {
        val teType = d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).get()
            .map {
                it.trackedEntityType()
            }.blockingGet()
        return if (d2.systemInfoModule().versionManager().is2_29) {
            d2.relationshipModule().relationshipTypes().blockingCount()
        } else {
            var items = 0
            d2.relationshipModule().relationshipTypes().withConstraints().blockingGet()
                .forEach { relationshipType ->
                    if (relationshipType.fromConstraint() != null &&
                        relationshipType.fromConstraint()!!.trackedEntityType() != null &&
                        relationshipType.fromConstraint()!!.trackedEntityType()!!.uid() == teType
                    ) {
                        if (relationshipType.toConstraint() != null &&
                            relationshipType.toConstraint()!!.trackedEntityType() != null
                        ) {
                            items += 1
                        }
                    } else if (relationshipType.bidirectional()!! &&
                        relationshipType.toConstraint() != null &&
                        relationshipType.toConstraint()!!.trackedEntityType() != null &&
                        relationshipType.toConstraint()!!.trackedEntityType()!!.uid() == teType
                    ) {
                        if (relationshipType.fromConstraint() != null &&
                            relationshipType.fromConstraint()!!.trackedEntityType() != null
                        ) {
                            items += 1
                        }
                    }
                }
            items
        }
    }
}

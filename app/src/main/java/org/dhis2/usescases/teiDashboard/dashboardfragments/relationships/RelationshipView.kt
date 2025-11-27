package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import org.dhis2.usescases.general.AbstractActivityContracts

interface RelationshipView : AbstractActivityContracts.View {
    fun goToAddRelationship(
        teiUid: String,
        teiTypeUidToAdd: String,
    )

    fun showPermissionError()

    fun openDashboardFor(teiUid: String)

    fun showTeiWithoutEnrollmentError(teiTypeName: String)

    fun showRelationshipNotFoundError(teiTypeName: String)

    fun openEventFor(
        eventUid: String,
        programUid: String,
    )
}

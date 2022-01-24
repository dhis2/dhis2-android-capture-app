package org.dhis2.data.forms.dataentry

import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.hisp.dhis.android.core.D2

class SearchTEIRepositoryImpl(val d2: D2, enrollmentUtils: DhisEnrollmentUtils) :
    SearchTEIRepository {
    override fun isUniqueTEIAttributeOnline(uid: String, value: String?, teiUid: String): Boolean {
        if (value == null) {
            return true
        }

        val localUid =
            d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet()!!
        val isUnique = localUid.unique() ?: false
        val orgUnitScope = localUid.orgUnitScope() ?: false

        if (isUnique){
            // Perform search with or without Org unit scope
        }

        return true
    }
}

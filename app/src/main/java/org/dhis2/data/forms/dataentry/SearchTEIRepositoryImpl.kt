package org.dhis2.data.forms.dataentry

import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode

class SearchTEIRepositoryImpl(
    private val d2: D2,
    private val enrollmentUtils: DhisEnrollmentUtils
) : SearchTEIRepository {

    override fun isUniqueTEIAttributeOnline(
        uid: String,
        value: String?,
        teiUid: String,
        programUid: String?
    ): Boolean {
        if (value == null || programUid == null) {
            return true
        }

        val attribute =
            d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet()!!
        val isUnique = attribute.unique() ?: false
        val orgUnitScope = attribute.orgUnitScope() ?: false

        if (isUnique && !orgUnitScope) {
            val teiList = d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache()
                .eq(true)
                .byOrgUnitMode()
                .eq(OrganisationUnitMode.ACCESSIBLE)
                .byProgram()
                .eq(programUid)
                .byAttribute(attribute.uid()).eq(value).blockingGet()

            if (teiList.isNullOrEmpty()) {
                return true
            }

            return teiList.none { it.uid() != teiUid }
        } else if (isUnique && orgUnitScope) {
            val orgUnit = enrollmentUtils.getOrgUnit(teiUid)

            val teiList = d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache()
                .eq(true)
                .byProgram()
                .eq(programUid)
                .byAttribute(attribute.uid())
                .eq(value)
                .byOrgUnitMode()
                .eq(OrganisationUnitMode.DESCENDANTS)
                .byOrgUnits()
                .`in`(orgUnit)
                .blockingGet()

            if (teiList.isNullOrEmpty()) {
                return true
            }

            return teiList.none { it.uid() != teiUid }
        }
        return true
    }
}

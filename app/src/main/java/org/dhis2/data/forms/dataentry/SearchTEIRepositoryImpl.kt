package org.dhis2.data.forms.dataentry

import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.commons.reporting.CrashReportControllerImpl
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute

class SearchTEIRepositoryImpl(
    private val d2: D2,
    private val enrollmentUtils: DhisEnrollmentUtils,
    private val crashController: CrashReportController = CrashReportControllerImpl(),
) : SearchTEIRepository {

    override fun isUniqueTEIAttributeOnline(
        uid: String,
        value: String?,
        teiUid: String,
        programUid: String?,
    ): Boolean {
        if (value == null || programUid == null) {
            return true
        }

        val attribute =
            d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet()!!
        val isUnique = attribute.unique() ?: false
        val orgUnitScope = attribute.orgUnitScope() ?: false

        if (isUnique && !orgUnitScope) {
            try {
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
            } catch (e: Exception) {
                return trackSentryError(e, programUid, attribute, value)
            }
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

    private fun trackSentryError(
        e: Exception,
        programUid: String?,
        attribute: TrackedEntityAttribute,
        value: String?,
    ): Boolean {
        val exception = if (e.cause != null && e.cause is D2Error) {
            val d2Error = e.cause as D2Error
            "component: ${d2Error.errorComponent()}," +
                " code: ${d2Error.errorCode()}," +
                " description: ${d2Error.errorDescription()}"
        } else {
            "No d2 Error"
        }
        crashController.addBreadCrumb(
            "SearchTEIRepositoryImpl.isUniqueAttribute",
            "programUid: $programUid ," +
                " attruid: ${attribute.uid()} ," +
                " attrvalue: $value, $exception",
        )
        return true
    }
}

package org.dhis2.form.data

import org.dhis2.commons.reporting.CrashReportController
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue

class UniqueAttributeController(
    private val d2: D2,
    private val crashReportController: CrashReportController
) {
    fun checkAttributeLocal(
        orgUnitScope: Boolean,
        teiUid: String,
        attributeUid: String,
        attributeValue: String
    ): Boolean {
        return if (!orgUnitScope) {
            val hasValue =
                getTrackedEntityAttributeValues(attributeUid, attributeValue, teiUid).isNotEmpty()
            !hasValue
        } else {
            val enrollingOrgUnit = getOrgUnit(teiUid)
            val hasValue = getTrackedEntityAttributeValues(attributeUid, attributeValue, teiUid)
                .map {
                    getOrgUnit(it.trackedEntityInstance()!!)
                }
                .all { it != enrollingOrgUnit }
            hasValue
        }
    }

    fun checkAttributeOnline(
        orgUnitScope: Boolean,
        programUid: String,
        teiUid: String,
        attributeUid: String,
        attributeValue: String
    ): Boolean {
        try {
            val teiList = teiCall(
                orgUnitScope,
                programUid,
                teiUid,
                attributeUid,
                attributeValue
            )

            if (teiList.isNullOrEmpty()) {
                return true
            }

            return teiList.none { it.uid() != teiUid }
        } catch (e: Exception) {
            return trackSentryError(e, programUid, attributeUid, attributeValue)
        }
    }

    private fun teiCall(
        orgUnitScope: Boolean,
        programUid: String,
        teiUid: String,
        attributeUid: String,
        attributeValue: String
    ) = if (orgUnitScope) {
        val orgUnit = getOrgUnit(teiUid)

        d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
            .allowOnlineCache().eq(true)
            .byProgram().eq(programUid)
            .byAttribute(attributeUid).eq(attributeValue)
            .byOrgUnitMode().eq(OrganisationUnitMode.DESCENDANTS)
            .byOrgUnits().`in`(orgUnit)
            .blockingGet()
    } else {
        d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
            .allowOnlineCache().eq(true)
            .byOrgUnitMode().eq(OrganisationUnitMode.ACCESSIBLE)
            .byProgram().eq(programUid)
            .byAttribute(attributeUid).eq(attributeValue)
            .blockingGet()
    }

    private fun getOrgUnit(teiUid: String): String? {
        return d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()
            .organisationUnit()
    }

    private fun trackSentryError(
        e: Exception,
        programUid: String?,
        attributeUid: String,
        value: String?
    ): Boolean {
        val exception = if (e.cause != null && e.cause is D2Error) {
            val d2Error = e.cause as D2Error
            "component: ${d2Error.errorComponent()}," +
                " code: ${d2Error.errorCode()}," +
                " description: ${d2Error.errorDescription()}"
        } else {
            "No d2 Error"
        }
        crashReportController.addBreadCrumb(
            "SearchTEIRepositoryImpl.isUniqueAttribute",
            "programUid: $programUid ," +
                " attruid: $attributeUid ," +
                " attrvalue: $value, $exception"
        )
        return true
    }

    private fun getTrackedEntityAttributeValues(
        uid: String,
        value: String,
        teiUid: String
    ): List<TrackedEntityAttributeValue> {
        return d2.trackedEntityModule().trackedEntityAttributeValues()
            .byTrackedEntityAttribute().eq(uid)
            .byTrackedEntityInstance().neq(teiUid)
            .byValue().eq(value).blockingGet()
    }
}

package org.dhis2.data.forms.dataentry

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.hisp.dhis.android.core.D2

class SearchTEIRepositoryImpl(
    private val d2: D2,
    private val enrollmentUtils: DhisEnrollmentUtils
) : SearchTEIRepository {

    /*
    //val tei = d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()

    d2.trackedEntityModule().trackedEntityInstanceQuery()
            .byProgram().eq(programUid)
     */

    val scope = CoroutineScope(Job() + Dispatchers.Main)

    override fun isUniqueTEIAttributeOnline(uid: String, value: String?, teiUid: String): Boolean {
        if (value == null) {
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
                .byAttribute(attribute.uid()).eq(value).blockingGet()

            if (teiList.isNullOrEmpty()) {
                return true
            }

            return teiList.none { it.uid() != teiUid }
        } else if (isUnique && orgUnitScope) {
            val orgUnit = enrollmentUtils.getOrgUnit(teiUid)

            val result = d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache()
                .eq(true)
                .byAttribute(attribute.uid())
                .eq(value)
                .byOrgUnits()
                .`in`(orgUnit)
                .blockingGet()
            return result.isNullOrEmpty()
        }
        return false
    }
}

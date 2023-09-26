package org.dhis2.metadata.usecases

import org.hisp.dhis.android.core.D2

class ProgramConfiguration(private val d2: D2) {

    fun getProgram(programUid: String) = d2.programModule().programs().uid(programUid).blockingGet()

    fun getProgramStyle(programUid: String) = getProgram(programUid)?.style()

    fun getProgramIcon(programUid: String) = getProgramStyle(programUid)?.icon()

    fun getProgramColor(programUid: String) = getProgramStyle(programUid)?.color()

    fun canEnrollNewTei(programUid: String): Boolean {
        val selectedProgram =
            d2.programModule().programs().uid(programUid).blockingGet()
        val programAccess = selectedProgram?.access()?.data()?.write() == true
        val teTypeAccess: Boolean = d2.trackedEntityModule().trackedEntityTypes().uid(
            selectedProgram?.trackedEntityType()?.uid(),
        ).blockingGet()?.access()?.data()?.write() == true
        return programAccess && teTypeAccess
    }
}

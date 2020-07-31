package org.dhis2.data.dhislogic

import javax.inject.Inject
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event

class DhisAccessUtils @Inject constructor(val d2: D2) {
    fun getEventAccessDataWrite(event: Event): Boolean {
        var canWrite: Boolean
        canWrite = d2.programModule().programs().uid(
            event.program()
        ).blockingGet().access().data().write()
        if (canWrite) canWrite = d2.programModule().programStages().uid(
            event.programStage()
        ).blockingGet().access().data().write()
        return canWrite
    }
}

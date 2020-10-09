package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import org.hisp.dhis.android.core.D2

fun getProgramStageName(d2: D2, eventUid: String): String {
    val event = d2.eventModule().events().uid(eventUid).blockingGet()
    val programStage = d2.programModule().programStages().uid(event.programStage()).blockingGet()

    return programStage?.displayName() ?: ""
}
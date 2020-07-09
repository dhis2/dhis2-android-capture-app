package org.dhis2.usecases.eventsWithoutRegistration.eventCapture

import org.hisp.dhis.android.core.D2
import java.util.HashMap

fun getProgramStageName(d2: D2, eventUid: String): String {
    val event = d2.eventModule().events().uid(eventUid).blockingGet()
    val programStage = d2.programModule().programStages().uid(event.programStage()).blockingGet()

    //TODO: retrieve attribute name if not exists get programStage
    val attValue = "Name:{{MBRvfOpzowH}} Name2:{{MBRvfOpzowH}}"

    return if (attValue.isNotBlank()) {
        val getDataElementDisplayName = { uid: String ->
            val teValue = d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, uid)
                .blockingGet()

            teValue?.value() ?: ""
        }

        getProgramStageNameByAttributeValue(attValue, getDataElementDisplayName)
    } else {
        programStage?.displayName() ?: ""
    }
}

fun getProgramStageNameByAttributeValue(
    attValue: String,
    getDataElementDisplayName: (uid: String) -> String
): String {
    val dataElementsMap: MutableMap<String, String> = HashMap()

    val matchResults = Regex("\\{\\{(.+?)\\}\\}").findAll(attValue)

    matchResults.forEach {
        val uidToken = it.value
        if (!dataElementsMap.containsKey(uidToken)) {
            val uid = uidToken.substring(2, it.value.length - 2)
            dataElementsMap[uidToken] = getDataElementDisplayName(uid)
        }
    }

    return dataElementsMap.entries.fold(attValue,
        { acc: String, (key, value) -> acc.replace(key, value) })
}
package org.dhis2.usecases.eventsWithoutRegistration.eventCapture

import org.dhis2.Bindings.userFriendlyValue
import org.hisp.dhis.android.core.D2
import java.util.HashMap

fun getProgramStageName(d2: D2, eventUid: String): String {
    val event = d2.eventModule().events().uid(eventUid).blockingGet()
    val programStage = d2.programModule().programStages().uid(event.programStage()).blockingGet()

    val attValue = getProgramStagePatternAttValue(d2,programStage.uid())

    return if (attValue.isNotBlank()) {
        val getDataElementDisplayName = { uid: String ->
            val teValue = d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, uid)
                .blockingGet()

            teValue.userFriendlyValue(d2) ?: ""
        }

        getProgramStageNameByAttributeValue(attValue, getDataElementDisplayName)
    } else {
        programStage?.displayName() ?: ""
    }
}

fun getProgramStagePatternAttValue(d2: D2, programStageUid: String): String {

    val attributeUid = getProgramStagePatternAttributeUid(d2)

    if (attributeUid.isNotBlank()) {
        val attributeValueSelect =
            "SELECT value FROM ProgramStageAttributeValueLink \n" +
                "WHERE attribute = ? AND programStage = ?"

        d2.databaseAdapter().rawQuery(attributeValueSelect, attributeUid, programStageUid)
            .use { cursor ->
                if (cursor != null && cursor.moveToFirst() && cursor.count > 0) {
                    return cursor.getString(0)
                }
            }
    }

    return "";
}

fun getProgramStagePatternAttributeUid(d2: D2): String {
    val attributeCode = "HeaderPattern"
    val attributeSelect = "SELECT uid FROM Attribute WHERE code = '${attributeCode}'";

    d2.databaseAdapter().rawQuery(attributeSelect).use { cursor ->
        if (cursor != null && cursor.moveToFirst() && cursor.count > 0) {
            return cursor.getString(0)
        }
    }

    return ""
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
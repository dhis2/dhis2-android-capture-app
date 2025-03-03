package org.dhis2.commons.intents

import android.content.Intent

enum class ActionType {
    FILL_DE,
    FILL_ATTR,
}

data class CustomIntentAction(
    val packageName: String,
    val arguments: Map<String, String>,
    val type: ActionType,
    val uid: String,
) {
    fun toIntent(): Intent {
        return Intent("$packageName.${type.name}").apply {
             // putExtra("uid", uid)
        }
    }
}

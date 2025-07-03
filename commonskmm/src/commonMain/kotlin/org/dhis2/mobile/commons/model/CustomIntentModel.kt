package org.dhis2.mobile.commons.model

data class CustomIntentModel(
    val uid: String,
    val name: String,
    val packageName: String,
    val customIntentRequest: List<CustomIntentRequestArgumentModel>,
    val customIntentResponse: CustomIntentResponseDataModel,
    val customIntentActionType: CustomIntentActionType,
)

data class CustomIntentTriggerModel(
    val dataElements: List<String>,
    val attributes: List<String>,
)

enum class CustomIntentActionType {
    DATA_ENTRY,
    SEARCH,
}

data class CustomIntentRequestArgumentModel(
    val key: String,
    val value: String,
)

data class CustomIntentResponseDataModel(
    val extra: String,
    val extraType: String,
    val keys: List<String>?,
)

enum class CustomIntentResponseExtraType {
    STRING,
    INTEGER,
    BOOLEAN,
    FLOAT,
    OBJECT,
    LIST_OF_OBJECTS,
}

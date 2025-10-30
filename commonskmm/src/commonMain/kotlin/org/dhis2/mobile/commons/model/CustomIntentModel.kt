package org.dhis2.mobile.commons.model

data class CustomIntentModel(
    val uid: String,
    val name: String?,
    val packageName: String,
    val customIntentRequest: List<CustomIntentRequestArgumentModel>,
    val customIntentResponse: List<CustomIntentResponseDataModel>,
)

data class CustomIntentRequestArgumentModel(
    val key: String,
    val value: Any,
)

data class CustomIntentResponseDataModel(
    val name: String,
    val extraType: CustomIntentResponseExtraType,
    val key: String?,
)

enum class CustomIntentResponseExtraType {
    STRING,
    INTEGER,
    BOOLEAN,
    FLOAT,
    OBJECT,
    LIST_OF_OBJECTS,
}

enum class CustomIntentActionTypeModel {
    DATA_ENTRY,
    SEARCH,
}

package org.dhis2.mobile.commons.customintents

import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.mobile.commons.model.CustomIntentModel

interface CustomIntentRepository {
    fun getCustomIntent(
        triggerUid: String?,
        orgUnitUid: String?,
        actionType: CustomIntentActionTypeModel,
    ): CustomIntentModel?

    fun attributeHasCustomIntentAndReturnsAListOfValues(
        triggerUid: String,
        actionType: CustomIntentActionTypeModel,
    ): Boolean

    fun reEvaluateCustomIntentRequestParams(
        orgUnitUid: String,
        customIntentUid: String,
    ): Map<String, Any?>
}

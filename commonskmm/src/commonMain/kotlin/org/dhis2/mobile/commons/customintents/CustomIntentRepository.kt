package org.dhis2.mobile.commons.customintents

import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.mobile.commons.model.CustomIntentModel

interface CustomIntentRepository {
    fun getCustomIntent(
        triggerUid: String?,
        orgunitUid: String?,
        actionType: CustomIntentActionTypeModel,
    ): CustomIntentModel?

    fun attributeHasCustomIntentAndReturnsAListOfValues(
        triggerUid: String,
        actionType: CustomIntentActionTypeModel,
    ): Boolean
}

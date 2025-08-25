package org.dhis2.mobile.commons.customintents

import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.mobile.commons.model.CustomIntentModel

interface CustomIntentRepository {
    fun getCustomIntent(
        triggerUid: String?,
        programUid: String?,
        programStageUid: String?,
        actionType: CustomIntentActionTypeModel,
    ): CustomIntentModel?
}

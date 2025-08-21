package org.dhis2.mobile.commons.customintents

import org.dhis2.mobile.commons.model.CustomIntentModel

interface CustomIntentProvider {
    fun getCustomIntentsWithTrigger(
        triggerUid: String?,
        programUid: String?,
        programStageUid: String?,
    ): CustomIntentModel?
}

package org.dhis2.mobile.commons.customintents

import org.dhis2.mobile.commons.model.CustomIntentModel

interface CustomIntentRepository {
    fun getCustomIntents(
        triggerUid: String?,
        programUid: String?,
        programStageUid: String?,
    ): CustomIntentModel?
}

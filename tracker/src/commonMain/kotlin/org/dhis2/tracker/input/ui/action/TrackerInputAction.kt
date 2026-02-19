package org.dhis2.tracker.input.ui.action

import org.dhis2.mobile.commons.model.CustomIntentModel

typealias CustomIntentUid = String
typealias FieldUid = String

sealed interface TrackerInputAction {
    data class LaunchCustomIntent(
        val fieldUid: FieldUid,
        val customIntentModel: CustomIntentModel,
    ) : TrackerInputAction
}

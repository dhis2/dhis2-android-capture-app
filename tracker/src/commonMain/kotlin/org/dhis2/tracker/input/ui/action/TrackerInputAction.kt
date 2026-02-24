package org.dhis2.tracker.input.ui.action

import org.dhis2.mobile.commons.model.CustomIntentModel
import org.dhis2.tracker.input.model.TrackerInputType

typealias CustomIntentUid = String
typealias FieldUid = String

sealed interface TrackerInputAction {
    data class LaunchCustomIntent(
        val fieldUid: FieldUid,
        val customIntentModel: CustomIntentModel,
    ) : TrackerInputAction

    data class Scan(
        val fieldUid: FieldUid,
        val optionSet: String?,
        val renderType: TrackerInputType,
    ) : TrackerInputAction

    data class ValueChanged(
        val fieldUid: FieldUid,
        val value: String?,
    ) : TrackerInputAction
}

package org.dhis2.usescases.searchTrackEntity

import org.dhis2.mobile.commons.model.CustomIntentModel
import org.dhis2.usescases.searchTrackEntity.searchparameters.FieldUid

sealed interface SearchAction {
    data class LaunchCustomIntent(
        val fieldUid: FieldUid,
        val customIntentModel: CustomIntentModel,
    ) : SearchAction
}

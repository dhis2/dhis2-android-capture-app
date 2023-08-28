package org.dhis2.data.dhislogic

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.FeatureType
import javax.inject.Inject

class DhisEventUtils @Inject constructor(val d2: D2) {
    fun newEventNeedsExtraInfo(eventUid: String): Boolean {
        val event = d2.eventModule().events().uid(eventUid)
            .blockingGet()
        val stage = d2.programModule().programStages().uid(event?.programStage())
            .blockingGet()
        val program = d2.programModule().programs().uid(stage?.program()?.uid())
            .blockingGet()
        val hasCoordinates = stage?.featureType() != null && stage.featureType() != FeatureType.NONE
        val hasNonDefaultCatCombo = d2.categoryModule().categoryCombos()
            .uid(program?.categoryComboUid()).blockingGet()?.isDefault != true
        return hasCoordinates || hasNonDefaultCatCombo
    }
}

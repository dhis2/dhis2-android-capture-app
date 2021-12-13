package org.dhis2.maps.model

import java.util.Date
import org.dhis2.commons.data.CarouselItemModel
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue

data class EventUiComponentModel(
    val eventUid: String,
    val event: Event,
    val enrollment: Enrollment,
    val programStage: ProgramStage?,
    val lastUpdated: Date?,
    val teiAttribute: LinkedHashMap<String, TrackedEntityAttributeValue?>,
    val teiImage: String,
    val teiDefaultIcon: String?,
    val orgUnitName: String
) : CarouselItemModel {
    override fun uid(): String = eventUid
    fun shouldShowNavigationButton(): Boolean {
        return event.geometry()?.type() == FeatureType.POINT
    }
}

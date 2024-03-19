package org.dhis2.maps.model

import org.dhis2.commons.data.CarouselItemModel
import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import java.util.Date

data class EventUiComponentModel(
    val eventUid: String,
    val event: Event,
    val enrollment: Enrollment,
    val programStage: ProgramStage?,
    val lastUpdated: Date?,
    val teiAttribute: LinkedHashMap<String, TrackedEntityAttributeValue?>,
    val teiImage: String,
    val teiDefaultIcon: String?,
    val orgUnitName: String,
    val metadataIconData: MetadataIconData,
) : CarouselItemModel {
    override fun uid(): String = eventUid
    fun shouldShowNavigationButton(): Boolean {
        return event.geometry()?.type() == FeatureType.POINT
    }
}

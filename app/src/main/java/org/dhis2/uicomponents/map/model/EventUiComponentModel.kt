package org.dhis2.uicomponents.map.model

import org.hisp.dhis.android.core.enrollment.Enrollment
import java.util.Date
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
    val teiDefaultIcon: String
) : CarouselItemModel

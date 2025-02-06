package org.dhis2.mobile.aggregates.data.mappers

import org.dhis2.mobile.aggregates.model.DataSetSection
import org.hisp.dhis.android.core.dataset.Section

fun Section.toDataSetSection() = DataSetSection(
    uid = uid(),
    title = displayName()!!,
)

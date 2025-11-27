package org.dhis2.mobile.aggregates.data.mappers

import org.dhis2.mobile.aggregates.model.DataSetSection
import org.hisp.dhis.android.core.dataset.Section

internal fun Section.toDataSetSection(misconfiguredRows: List<String>) =
    DataSetSection(
        uid = uid(),
        title = displayName()!!,
        topContent = displayOptions()?.beforeSectionText(),
        bottomContent = displayOptions()?.afterSectionText(),
        misconfiguredRows = misconfiguredRows,
    )

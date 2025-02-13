package org.dhis2.mobile.aggregates.data.mappers

import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.hisp.dhis.android.core.dataset.DataSetInstance

internal fun DataSetInstance.toDataSetDetails(isDefaultCatCombo: Boolean) = DataSetDetails(
    titleLabel = this.dataSetDisplayName(),
    dateLabel = this.period(),
    orgUnitLabel = this.organisationUnitDisplayName(),
    catOptionComboLabel = if (isDefaultCatCombo) {
        null
    } else {
        this.attributeOptionComboDisplayName()
    },
)

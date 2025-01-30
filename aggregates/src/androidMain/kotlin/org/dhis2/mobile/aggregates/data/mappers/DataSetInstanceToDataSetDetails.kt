package org.dhis2.mobile.aggregates.data.mappers

import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.hisp.dhis.android.core.dataset.DataSetInstance

fun DataSetInstance.toDataSetDetails() = DataSetDetails(
    titleLabel = this.dataSetDisplayName(),
    dateLabel = this.period(),
    orgUnitLabel = this.organisationUnitDisplayName(),
    catOptionComboLabel = this.attributeOptionComboDisplayName(),
)

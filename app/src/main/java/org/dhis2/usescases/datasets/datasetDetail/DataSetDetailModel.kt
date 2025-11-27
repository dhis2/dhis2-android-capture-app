package org.dhis2.usescases.datasets.datasetDetail

import org.hisp.dhis.android.core.common.State
import java.util.Date

data class DataSetDetailModel(
    val datasetUid: String,
    val orgUnitUid: String,
    val catOptionComboUid: String,
    val periodId: String,
    val nameOrgUnit: String,
    val nameCatCombo: String,
    val namePeriod: String,
    val state: State,
    val periodType: String,
    val displayOrgUnitName: Boolean,
    val isComplete: Boolean,
    val lastUpdated: Date,
    val nameCategoryOptionCombo: String,
)

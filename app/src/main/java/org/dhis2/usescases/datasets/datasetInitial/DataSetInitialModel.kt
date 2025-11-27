package org.dhis2.usescases.datasets.datasetInitial

import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.period.PeriodType

data class DataSetInitialModel(
    val displayName: String,
    val description: String?,
    val categoryCombo: String,
    val categoryComboName: String,
    val periodType: PeriodType,
    val categories: List<Category?>,
    val openFuturePeriods: Int?,
)

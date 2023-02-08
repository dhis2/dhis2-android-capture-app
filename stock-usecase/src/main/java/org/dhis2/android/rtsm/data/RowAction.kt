package org.dhis2.android.rtsm.data

import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.android.rtsm.ui.base.OnQuantityValidated

data class RowAction(
    val entry: StockEntry,
    val position: Int,
    val callback: OnQuantityValidated?
)

package org.dhis2.android.rtsm.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.android.rtsm.data.models.Transaction

@Parcelize
data class ReviewStockData(
    val transaction: Transaction,
    val items: List<StockEntry>
): Parcelable
package org.dhis2.android.rtsm.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StockEntry(
    val item: StockItem,
    var qty: String? = null,
    var stockOnHand: String? = null,
    var hasError: Boolean = false
): Parcelable
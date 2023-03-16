package org.dhis2.android.rtsm.data.models

import android.os.Parcelable
import java.util.Calendar
import java.util.Date
import kotlinx.parcelize.Parcelize

@Parcelize
data class StockEntry(
    val item: StockItem,
    var qty: String? = null,
    var stockOnHand: String? = null,
    var errorMessage: String? = null,
    val date: Date = Calendar.getInstance().time
) : Parcelable

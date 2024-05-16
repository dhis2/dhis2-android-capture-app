package org.dhis2.android.rtsm.data.models

import org.dhis2.android.rtsm.data.TransactionType

data class TransactionItem(
    val icon: Int,
    val type: TransactionType,
    var label: String,
)

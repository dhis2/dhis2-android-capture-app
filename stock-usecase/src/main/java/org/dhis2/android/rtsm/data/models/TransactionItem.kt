package org.dhis2.android.rtsm.data.models

data class TransactionItem(
    val icon: Int,
    val type: TransactionType,
    var label: String,
)

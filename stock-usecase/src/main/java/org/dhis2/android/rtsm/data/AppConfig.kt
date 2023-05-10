package org.dhis2.android.rtsm.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppConfig(
    val program: String,
    val itemCode: String,
    val itemName: String,
    val stockOnHand: String,
    val distributedTo: String,
    val stockDistribution: String,
    val stockCount: String,
    val stockDiscarded: String
) : Parcelable

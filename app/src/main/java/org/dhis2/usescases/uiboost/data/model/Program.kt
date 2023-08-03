package org.dhis2.usescases.uiboost.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Program(
    val hidden: String,
    val icon: String,
    val program: String,
    val programGroup: String
)
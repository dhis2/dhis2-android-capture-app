package org.dhis2.usescases.uiboost.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProgramGroup(
    val key: String,
    val order: Int,
    val style: String
)
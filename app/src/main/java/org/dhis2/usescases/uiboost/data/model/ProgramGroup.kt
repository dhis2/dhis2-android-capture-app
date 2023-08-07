package org.dhis2.usescases.uiboost.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProgramGroup(
    val label: String,
    val order: Int,
    val programs: List<Program>,
    val style: String
)
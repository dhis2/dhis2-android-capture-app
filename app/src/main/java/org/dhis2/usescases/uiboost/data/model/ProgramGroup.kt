package org.dhis2.usescases.uiboost.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.serialization.Serializable

@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProgramGroup(
    val label: String,
    val order: Int,
    val programs: List<Program>,
    val style: String
)

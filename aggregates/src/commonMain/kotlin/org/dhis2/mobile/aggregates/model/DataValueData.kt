package org.dhis2.mobile.aggregates.model

internal data class DataValueData(
    val value: String?,
    val conflicts: Conflicts,
    val legendColor: String?,
)

internal data class Conflicts(
    private val errors: List<String>,
    private val warnings: List<String>,
) {
    fun errors() =
        errors
            .joinToString(
                separator = ".\n",
            ).takeIf { it.isNotEmpty() }

    fun warnings() =
        warnings
            .joinToString(
                separator = ".\n",
            ).takeIf { it.isNotEmpty() }
}

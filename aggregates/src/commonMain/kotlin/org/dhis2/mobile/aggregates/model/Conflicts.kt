package org.dhis2.mobile.aggregates.model

data class Conflicts(
    private val errors: List<String>,
    private val warnings: List<String>,
) {
    fun errors() = errors.joinToString(
        separator = ".\n",
    ).takeIf { it.isNotEmpty() }

    fun warnings() = warnings.joinToString(
        separator = ".\n",
    ).takeIf { it.isNotEmpty() }
}

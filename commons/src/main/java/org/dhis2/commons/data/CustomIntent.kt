package org.dhis2.commons.data

data class CustomIntent(
    val arguments: Map<String, String>,
    val intentId: String,
    val name: String,
    val packageName: String,
    val targets: Targets
)

data class CustomIntents(
    val customIntents: List<CustomIntent>
)

data class Targets(
    val attributes: List<String>,
    val dataElements: List<String>
)
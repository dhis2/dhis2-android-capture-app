package org.dhis2.mobile.commons.model.internal

data class DomainOrgUnit(
    val uid: String,
    val label: String,
    val level: Int,
    val path: String,
    val namePath: List<String>,
)

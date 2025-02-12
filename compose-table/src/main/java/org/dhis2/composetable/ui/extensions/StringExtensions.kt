package org.dhis2.composetable.ui.extensions

fun String?.isNumeric() = this?.toDoubleOrNull() != null

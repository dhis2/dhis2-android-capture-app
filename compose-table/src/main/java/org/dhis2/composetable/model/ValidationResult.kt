package org.dhis2.composetable.model

sealed class ValidationResult {
    data class Success(val value: String?) : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

package org.dhis2.mobile.commons.error

sealed class DomainError : Throwable() {
    data class NetworkError(
        override val message: String,
    ) : DomainError()

    data class ServerError(
        override val message: String,
    ) : DomainError()

    data class UnknownError(
        override val message: String,
    ) : DomainError()

    data class DataBaseError(
        override val message: String,
    ) : DomainError()
}

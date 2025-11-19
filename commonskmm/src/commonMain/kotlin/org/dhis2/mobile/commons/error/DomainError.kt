package org.dhis2.mobile.commons.error

/**
 * A sealed class representing different types of domain errors that can occur in the application.
 * Errors are categorized by their nature and typical handling strategy in the UI layer.
 *
 * The main categories are:
 * - Authentication & Access: User login, permissions, and access control issues
 * - Network & Server: Connectivity and server communication problems
 * - Data Operations: Validation, storage, and data integrity issues
 * - Configuration: App or system misconfiguration
 * - Generic: Unexpected errors with no specific category
 */
sealed class DomainError : Throwable() {
    abstract override val message: String

    // ==================== Authentication & Access ====================

    /**
     * Authentication failure (login, password, credentials).
     * Typically requires user to re-authenticate.
     */
    data class AuthenticationError(
        override val message: String,
    ) : DomainError()

    /**
     * User lacks authorization for this operation (program/feature not accessible).
     * Different from PermissionDeniedError: this is about user rights/enrollment in programs.
     * Typically shows access denied message and suggests contacting admin.
     */
    data class UnauthorizedAccessError(
        override val message: String,
    ) : DomainError()

    /**
     * User account has been denied access (disabled, locked, etc).
     * Typically requires admin intervention.
     */
    data class PermissionDeniedError(
        override val message: String,
    ) : DomainError()

    // ==================== Network & Server ====================

    /**
     * Network connectivity issue (no internet, timeout, connection refused).
     * Typically retryable when network is available.
     */
    data class NetworkError(
        override val message: String,
    ) : DomainError()

    /**
     * Server-side error (500+, service unavailable).
     * Typically retryable after server recovery.
     */
    data class ServerError(
        override val message: String,
    ) : DomainError()

    /**
     * API-level error (invalid query, malformed request, parsing).
     * Typically requires code fix or data adjustment.
     */
    data class ApiError(
        override val message: String,
    ) : DomainError()

    // ==================== Data Operations ====================

    /**
     * Data validation failure (invalid characters, constraints violated).
     * Typically requires user to fix input data.
     */
    data class DataValidationError(
        override val message: String,
    ) : DomainError()

    /**
     * Database operation failure (insert, update, export, import).
     * Typically requires retry or data cleanup.
     */
    data class DatabaseError(
        override val message: String,
    ) : DomainError()

    /**
     * Requested data not found in storage.
     * Typically non-recoverable, may indicate data sync issues.
     */
    data class DataNotFoundError(
        override val message: String,
    ) : DomainError()

    // ==================== Configuration ====================

    /**
     * App or system misconfiguration (app name not set, settings unavailable).
     * Typically requires admin setup before operation.
     */
    data class ConfigurationError(
        override val message: String,
    ) : DomainError()

    // ==================== Generic ====================

    /**
     * Unexpected error that doesn't fit other categories.
     * Fallback for errors that shouldn't normally occur.
     */
    data class UnexpectedError(
        override val message: String,
    ) : DomainError()
}

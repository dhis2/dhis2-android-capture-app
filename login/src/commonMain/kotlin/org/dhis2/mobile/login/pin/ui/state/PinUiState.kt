package org.dhis2.mobile.login.pin.ui.state

/**
 * UI state for the PIN dialog content.
 * Exposed by [org.dhis2.mobile.login.pin.ui.viewmodel.PinViewModel] and consumed by composables.
 *
 * @param title The main heading text.
 * @param subtitle The descriptive text shown below the title.
 * @param primaryButtonText Label for the primary action button.
 * @param secondaryButtonText Optional label for the secondary action button.
 * @param errorMessage Optional error message shown below the PIN input field.
 * @param primaryButtonIsEnabled Whether the primary action button is enabled.
 * @param isLoading Whether a PIN operation is in progress.
 * @param isSuccess Whether the PIN operation completed successfully.
 * @param isDismissed Whether the PIN dialog was dismissed (e.g. after forgot-PIN flow).
 * @param isTooManyAttempts Whether the user exceeded the maximum number of PIN attempts.
 * @param pinLength Number of PIN digits.
 */
data class PinUiState(
    val title: String = "",
    val subtitle: String = "",
    val primaryButtonText: String = "",
    val secondaryButtonText: String? = null,
    val errorMessage: String? = null,
    val primaryButtonIsEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isDismissed: Boolean = false,
    val isTooManyAttempts: Boolean = false,
    val pinLength: Int = 4,
)

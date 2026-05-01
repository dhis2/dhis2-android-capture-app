package org.dhis2.mobile.login.pin.domain.model

/**
 * PIN mode enumeration for different bottom sheet behaviors.
 */
enum class PinMode {
    /**
     * SET mode - Used when creating/setting a new PIN.
     */
    SET,

    /**
     * ASK mode - Used when verifying/entering an existing PIN.
     */
    ASK,
}

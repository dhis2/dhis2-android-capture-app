package org.dhis2.usescases.settings

class GatewayValidator {
    private val regex = Regex("^\\+[1-9][0-9]{3,16}\$")
    private val maxSize = 16

    sealed class GatewayValidationResult {
        data object Empty : GatewayValidationResult()

        data object Valid : GatewayValidationResult()

        data object Invalid : GatewayValidationResult()
    }

    operator fun invoke(text: String): GatewayValidationResult =
        when {
            text.isEmpty() -> GatewayValidationResult.Empty
            text.matches(regex) && !plusIsMissingOrIsTooLong(text) -> GatewayValidationResult.Valid
            else -> GatewayValidationResult.Invalid
        }

    private fun plusIsMissingOrIsTooLong(gateway: String): Boolean =
        !gateway.startsWith("+") && gateway.length == 1 || gateway.length > maxSize
}

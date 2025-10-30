package org.dhis2.mobile.login.main.domain.model

data class BiometricsInfo(
    val canUseBiometrics: Boolean,
    val displayBiometricsMessageAfterLogin: Boolean,
)

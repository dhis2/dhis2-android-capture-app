package org.dhis2.mobile.commons.biometrics

import coil3.PlatformContext
import javax.crypto.Cipher

interface BiometricActions {
    fun hasBiometric(): Boolean

    context(context: PlatformContext)
    fun authenticate(
        cipher: Cipher,
        callback: (Cipher) -> Unit,
    )
}

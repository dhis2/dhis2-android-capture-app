package org.dhis2.mobile.commons.biometrics

import javax.crypto.Cipher

interface BiometricActions {

    fun hasBiometric(): Boolean
    fun authenticate(cipher: Cipher, callback:(Cipher)-> Unit)
}
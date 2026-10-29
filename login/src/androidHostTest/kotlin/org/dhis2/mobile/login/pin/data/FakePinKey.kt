package org.dhis2.mobile.login.pin.data

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** JVM stand-in for [AndroidKeystorePinKey]: each [deviceSecret] behaves like a different device. */
fun fakePinKey(deviceSecret: String = "device-secret") =
    PinKey { data, _ ->
        Mac.getInstance("HmacSHA256").run {
            init(SecretKeySpec(deviceSecret.encodeToByteArray(), "HmacSHA256"))
            doFinal(data)
        }
    }

/** A device whose Keystore key is gone. */
val missingPinKey = PinKey { _, _ -> null }

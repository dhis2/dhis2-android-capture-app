package org.dhis2.extensions

import org.dhis2.utils.security.AESUtils


fun ByteArray.encrypt(password: String): ByteArray {
    return AESUtils.encryptMsg(this, password)
}

fun ByteArray.decrypt(password: String): ByteArray {
   return AESUtils.decryptMsg(this, password)
}
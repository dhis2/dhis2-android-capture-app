package org.dhis2.data.fingerprint

import co.infinum.goldfinger.Goldfinger

class FingerPrintMapper {
    fun mapFromGoldFingerToFingerPrint(result: Goldfinger.Result) = when (result.type()) {
        Goldfinger.Type.SUCCESS -> FingerPrintResult(Type.SUCCESS, result.message())
        Goldfinger.Type.INFO -> FingerPrintResult(Type.INFO, result.message())
        else -> FingerPrintResult(Type.ERROR, result.message())
    }
}

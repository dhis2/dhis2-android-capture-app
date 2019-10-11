package org.dhis2.data.fingerprint

import co.infinum.goldfinger.Goldfinger

class FingerPrintMapper(){
    fun mapFromGoldFingerToFingerPrint(result: Goldfinger.Result)
        = when(result.type()){
             Goldfinger.Type.SUCCESS -> FingerPrintResult(Type.SUCCESS)
             Goldfinger.Type.INFO -> FingerPrintResult(Type.INFO)
        else -> FingerPrintResult(Type.ERROR)
    }
}
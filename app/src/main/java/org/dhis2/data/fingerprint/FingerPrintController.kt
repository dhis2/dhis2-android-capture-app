package org.dhis2.data.fingerprint

import io.reactivex.Observable

interface FingerPrintController {
    fun hasFingerPrint() : Boolean
    fun authenticate() : Observable<FingerPrintResult>
    fun cancel()
}
package org.dhis2.data.fingerprint

import io.reactivex.Observable

class FingerPrintControllerImpl(
    val mapper: FingerPrintMapper
) : FingerPrintController {

    /***
     *Checks if device supports fingerprint hardware
     * */
    override fun hasFingerPrint(): Boolean {
        return false
    }

    /***
     *Auth using fingerprint and map to result
     * */
    override fun authenticate(): Observable<FingerPrintResult> {
        return Observable.empty()
    }

    override fun cancel() {
    }
}

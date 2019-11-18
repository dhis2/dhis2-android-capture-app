package org.dhis2.data.fingerprint

import co.infinum.goldfinger.rx.RxGoldfinger
import io.reactivex.Observable

class FingerPrintControllerImpl(val goldfinger: RxGoldfinger, val mapper: FingerPrintMapper) :
    FingerPrintController {

    override fun hasFingerPrint(): Boolean {
        return goldfinger.hasEnrolledFingerprint()
    }

    override fun authenticate(): Observable<FingerPrintResult> {
        return goldfinger.authenticate().map {
            mapper.mapFromGoldFingerToFingerPrint(it)
        }
    }

    override fun cancel() {
        goldfinger.cancel()
    }
}

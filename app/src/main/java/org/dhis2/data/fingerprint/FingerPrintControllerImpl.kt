package org.dhis2.data.fingerprint

import co.infinum.goldfinger.Goldfinger
import co.infinum.goldfinger.rx.RxGoldfinger
import io.reactivex.Observable

class FingerPrintControllerImpl(
    val goldfinger: RxGoldfinger,
    val mapper: FingerPrintMapper
) :
    FingerPrintController {

    override fun hasFingerPrint(): Boolean {
        return goldfinger.hasFingerprintHardware() && goldfinger.hasEnrolledFingerprint()
    }

    override fun authenticate(
        promptParams: Goldfinger.PromptParams
    ): Observable<FingerPrintResult> {
        return goldfinger.authenticate(promptParams).map {
            mapper.mapFromGoldFingerToFingerPrint(it)
        }
    }

    override fun cancel() {
        goldfinger.cancel()
    }
}

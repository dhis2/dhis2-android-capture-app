package org.dhis2.usescases.qrScanner

import dagger.Subcomponent
import org.dhis2.data.dagger.PerActivity

@PerActivity
@Subcomponent(modules = [ScanModule::class])
interface ScanComponent {
    fun inject(scanActivity: ScanActivity?)
}
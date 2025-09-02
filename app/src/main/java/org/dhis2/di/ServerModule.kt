package org.dhis2.di

import org.hisp.dhis.android.core.D2Configuration
import org.hisp.dhis.android.core.D2Manager
import org.koin.dsl.module

fun serverModule(d2Configuration: D2Configuration) =
    module {
        single {
            if (!D2Manager.isD2Instantiated()) {
                D2Manager
                    .blockingInstantiateD2(d2Configuration)
                    ?.also {
                        it.userModule().accountManager().setMaxAccounts(null)
                    }!!
            } else {
                D2Manager.getD2()
            }
        }
    }

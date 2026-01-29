package org.dhis2.commons.di

import org.dhis2.commons.periods.data.PeriodLabelProvider
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val resourceManagerModule =
    module {
        single {
            ColorUtils()
        }

        single {
            ResourceManager(androidContext(), get())
        }

        factory {
            PeriodLabelProvider()
        }
    }

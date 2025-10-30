package org.dhis2.commons.di

import org.dhis2.commons.periods.data.PeriodLabelProvider
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.koin.dsl.module

val resourceManagerModule =
    module {
        single {
            ColorUtils()
        }
        factory { params ->
            ResourceManager(params.get(), get())
        }

        factory {
            PeriodLabelProvider()
        }
    }

package org.dhis2.usescases.settingsprogram.di

import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.usescases.settingsprogram.SettingsProgramViewModel
import org.dhis2.usescases.settingsprogram.data.SettingsProgramRepository
import org.dhis2.usescases.settingsprogram.domain.GetProgramSpecificSettings
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsProgramModule =
    module {

        factory {
            MetadataIconProvider(get())
        }

        single {
            SettingsProgramRepository(get())
        }

        single {
            GetProgramSpecificSettings(get(), get(), get())
        }

        viewModel {
            SettingsProgramViewModel(get())
        }
    }

package org.dhis2.mobile.commons.di

import org.dhis2.mobile.commons.data.TableDimensionRepository
import org.dhis2.mobile.commons.data.TableDimensionRepositoryImpl
import org.dhis2.mobile.commons.data.ValueParser
import org.dhis2.mobile.commons.data.ValueParserImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val commonsModule: Module
    get() = module {
        single<ValueParser> {
            ValueParserImpl(get())
        }
        factory<TableDimensionRepository> { params ->
            TableDimensionRepositoryImpl(get(), params.get())
        }
    }

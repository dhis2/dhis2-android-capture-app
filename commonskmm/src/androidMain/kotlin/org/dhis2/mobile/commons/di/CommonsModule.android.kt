package org.dhis2.mobile.commons.di

import org.dhis2.mobile.commons.data.TableDimensionRepository
import org.dhis2.mobile.commons.data.TableDimensionRepositoryImpl
import org.dhis2.mobile.commons.data.ValueParser
import org.dhis2.mobile.commons.data.ValueParserImpl
import org.dhis2.mobile.commons.files.FileController
import org.dhis2.mobile.commons.files.FileControllerImpl
import org.dhis2.mobile.commons.files.FileHandler
import org.dhis2.mobile.commons.files.FileHandlerImpl
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.mobile.commons.reporting.CrashReportControllerImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val commonsModule: Module
    get() = module {
        single<ValueParser> {
            ValueParserImpl(get())
        }
        single<FileController> {
            FileControllerImpl()
        }
        single<FileHandler> {
            FileHandlerImpl()
        }
        single<CrashReportController> {
            CrashReportControllerImpl()
        }
        factory<TableDimensionRepository> { params ->
            TableDimensionRepositoryImpl(get(), params.get())
        }
    }

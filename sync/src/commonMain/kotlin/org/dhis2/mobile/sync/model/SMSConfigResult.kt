package org.dhis2.mobile.sync.model

sealed interface SMSConfigResult {
    data object EnableModule : SMSConfigResult

    data object DisableModule : SMSConfigResult

    data object DoNothing : SMSConfigResult
}

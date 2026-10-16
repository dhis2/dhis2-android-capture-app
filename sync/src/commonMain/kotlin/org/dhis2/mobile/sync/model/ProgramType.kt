package org.dhis2.mobile.sync.model

sealed interface ProgramType {
    data object Event : ProgramType

    data object Tracker : ProgramType

    data object None : ProgramType
}

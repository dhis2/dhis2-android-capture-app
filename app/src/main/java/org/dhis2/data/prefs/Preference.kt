package org.dhis2.data.prefs

class Preference {
    companion object {
        const val INITIAL_SYNC_DONE = "INITIAL_SYNC_DONE"
        const val SESSION_LOCKED = "SessionLocked"
        const val PIN = "pin"

        // SYNC PARAMETERS
        const val EVENT_MAX = "EVENT_MAX"
        const val TEI_MAX = "TEI_MAX"
        const val LIMIT_BY_ORG_UNIT = "LIMIT_BY_ORG_UNIT"
        const val LIMIT_BY_PROGRAM = "LIMIT_BY_PROGRAM"
        const val EVENT_MAX_DEFAULT = 1000
        const val TEI_MAX_DEFAULT = 500
        const val TIME_META = "timeMeta"
        const val TIME_DATA = "timeData"
        const val INITIAL_SYNC = "INITIAL_SYNC"
        const val META = "METADATA"
        const val META_NOW = "METADATA_NOW"
        const val DATA = "DATA"
        const val DATA_NOW = "DATA_NOW"

        // METADATA SYNC TIME RANGES
        const val TIME_DAILY = 86400
        const val TIME_WEEKLY = 604800
        const val TIME_MANUAL = 0

        // DATA SYNC TIME RANGES
        const val TIME_15M = 15 * 60
        const val TIME_HOURLY = 60 * 60

        const val DEFAULT_CAT_COMBO = "DEFAULT_CAT_COMB"
        const val PREF_DEFAULT_CAT_OPTION_COMBO = "PREF_DEFAULT_CAT_OPTION_COMBO"

        const val NUMBER_RV = "pref_rv"
        const val DEFAULT_NUMBER_RV = 100

        const val GROUPING = "GROUPING"

        const val CURRENT_ORG_UNIT = "CURRENT_ORG_UNIT"
    }
}

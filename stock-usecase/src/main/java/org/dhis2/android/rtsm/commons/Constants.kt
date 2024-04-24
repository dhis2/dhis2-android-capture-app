package org.dhis2.android.rtsm.commons

object Constants {
    const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT = "yyyy-MM-dd"

    const val ITEM_PAGE_SIZE = 20

    const val SEARCH_QUERY_DEBOUNCE = 300L
    const val QUANTITY_ENTRY_DEBOUNCE = 500L

    // Intent Extras
    const val INTENT_EXTRA_APP_CONFIG = "APP_CONFIG"
    const val INTENT_EXTRA_MESSAGE = "MESSAGE"

    // Metadata & Data sync periods
    const val PERIOD_DAILY = 24 * 60 * 60
    const val PERIOD_WEEKLY = 7 * 24 * 60 * 60
    const val PERIOD_MANUAL = 0
    const val PERIOD_30M = 30 * 60
    const val PERIOD_1H = 60 * 60
    const val PERIOD_6H = 6 * 60 * 60
    const val PERIOD_12H = 12 * 60 * 60

    const val AUDIO_RECORDING_REQUEST_CODE = 105
    const val MAX_ALLOWABLE_DAYS_BACK_RANGE = -5

    const val NON_NUMERIC_SPEECH_INPUT_ERROR = 991865
    const val NEGATIVE_NUMBER_NOT_ALLOWED_INPUT_ERROR = 991866
}

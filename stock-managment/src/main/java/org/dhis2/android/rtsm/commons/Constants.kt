package org.dhis2.android.rtsm.commons

object Constants {
    const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT = "yyyy-MM-dd"

    //Preferences
    const val SHARED_PREFS = "icrc_psm_shared_prefs"

    const val SERVER_URL = "SERVER_URL"
    const val USERNAME = "USERNAME"
    const val PASSWORD = "PASSWORD"

    const val ITEM_PAGE_SIZE = 20
    const val USER_ACTIVITY_COUNT = 8

    // Configuration file keys
    const val CONFIG_PROGRAM = "program"
    const val CONFIG_ITEM_CODE = "item_code"
    const val CONFIG_ITEM_VALUE = "item_value"
    const val CONFIG_STOCK_ON_HAND = "stock_on_hand"
    const val CONFIG_DE_DISTRIBUTED_TO = "de_distributed_to"
    const val CONFIG_DE_STOCK_DISTRIBUTION = "de_stock_distributed"
    const val CONFIG_DE_STOCK_CORRECTION = "de_stock_correction"
    const val CONFIG_DE_STOCK_DISCARD = "de_stock_discarded"

    const val SEARCH_QUERY_DEBOUNCE = 300L
    const val QUANTITY_ENTRY_DEBOUNCE = 500L
    const val SCREEN_TRANSITION_DELAY = 100L
    const val CLEAR_FIELD_DELAY = 3000L // A Toast clears within 4s or 7s

    // Intent Extras
    const val INTENT_EXTRA_APP_CONFIG = "APP_CONFIG"
    const val INTENT_EXTRA_MESSAGE = "MESSAGE"
    const val INTENT_EXTRA_TRANSACTION = "TRANSACTION_CHOICES"
    const val INTENT_EXTRA_STOCK_ENTRIES = "STOCK_ENTRIES"

    // Sync parameters
    const val INITIAL_SYNC = "INITIAL_SYNC"
    const val INSTANT_METADATA_SYNC = "INSTANT_METADATA_SYNC"
    const val INSTANT_DATA_SYNC = "INSTANT_DATA_SYNC"
    const val SCHEDULED_METADATA_SYNC = "SCHEDULED_METADATA_SYNC"
    const val SCHEDULED_DATA_SYNC = "SCHEDULED_DATA_SYNC"
    const val LAST_DATA_SYNC_DATE = "LAST_DATA_SYNC_DATE"
    const val LAST_DATA_SYNC_STATUS = "LAST_DATA_SYNC_STATUS"
    const val LAST_DATA_SYNC_RESULT = "LAST_DATA_SYNC_RESULT"
    const val LAST_METADATA_SYNC_DATE = "LAST_METADATA_SYNC_DATE"
    const val LAST_METADATA_SYNC_STATUS = "LAST_METADATA_SYNC_STATUS"
    const val SYNC_PERIOD_METADATA = "SYNC_PERIOD_METADATA"
    const val SYNC_PERIOD_DATA = "SYNC_PERIOD_DATA"
    const val SYNC_DATA_NOTIFICATION_CHANNEL = "SYNC_DATA_CHANNEL"
    const val SYNC_DATA_CHANNEL_NAME = "DATA_SYNC"
    const val SYNC_DATA_NOTIFICATION_ID = 710776
    const val SYNC_METADATA_NOTIFICATION_CHANNEL = "SYNC_METADATA_CHANNEL"
    const val SYNC_METADATA_CHANNEL_NAME = "METADATA_SYNC"
    const val SYNC_METADATA_NOTIFICATION_ID = 893455
    const val WORKER_ERROR_MESSAGE_KEY = "ERROR_MESSAGE"

    // Metadata & Data sync periods
    const val PERIOD_DAILY = 24 * 60 * 60
    const val PERIOD_WEEKLY = 7 * 24 * 60 * 60
    const val PERIOD_MANUAL = 0
    const val PERIOD_30M = 30 * 60
    const val PERIOD_1H = 60 * 60
    const val PERIOD_6H = 6 * 60 * 60
    const val PERIOD_12H = 12 * 60 * 60

    const val AUDIO_RECORDING_REQUEST_CODE = 105
    const val CLEAR_ICON = 0
    const val MAX_ALLOWABLE_DAYS_BACK_RANGE = -5

    const val NON_NUMERIC_SPEECH_INPUT_ERROR = 991865
    const val NEGATIVE_NUMBER_NOT_ALLOWED_INPUT_ERROR = 991866
}
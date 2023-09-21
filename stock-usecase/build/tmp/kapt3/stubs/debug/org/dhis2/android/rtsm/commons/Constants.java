package org.dhis2.android.rtsm.commons;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000f\n\u0002\u0010\t\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0016X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lorg/dhis2/android/rtsm/commons/Constants;", "", "()V", "AUDIO_RECORDING_REQUEST_CODE", "", "DATETIME_FORMAT", "", "DATE_FORMAT", "INTENT_EXTRA_APP_CONFIG", "INTENT_EXTRA_MESSAGE", "ITEM_PAGE_SIZE", "MAX_ALLOWABLE_DAYS_BACK_RANGE", "NEGATIVE_NUMBER_NOT_ALLOWED_INPUT_ERROR", "NON_NUMERIC_SPEECH_INPUT_ERROR", "PERIOD_12H", "PERIOD_1H", "PERIOD_30M", "PERIOD_6H", "PERIOD_DAILY", "PERIOD_MANUAL", "PERIOD_WEEKLY", "QUANTITY_ENTRY_DEBOUNCE", "", "SEARCH_QUERY_DEBOUNCE", "psm-v2.9-DEV_debug"})
public final class Constants {
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String DATE_FORMAT = "yyyy-MM-dd";
    public static final int ITEM_PAGE_SIZE = 20;
    public static final long SEARCH_QUERY_DEBOUNCE = 300L;
    public static final long QUANTITY_ENTRY_DEBOUNCE = 500L;
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String INTENT_EXTRA_APP_CONFIG = "APP_CONFIG";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String INTENT_EXTRA_MESSAGE = "MESSAGE";
    public static final int PERIOD_DAILY = 86400;
    public static final int PERIOD_WEEKLY = 604800;
    public static final int PERIOD_MANUAL = 0;
    public static final int PERIOD_30M = 1800;
    public static final int PERIOD_1H = 3600;
    public static final int PERIOD_6H = 21600;
    public static final int PERIOD_12H = 43200;
    public static final int AUDIO_RECORDING_REQUEST_CODE = 105;
    public static final int MAX_ALLOWABLE_DAYS_BACK_RANGE = -5;
    public static final int NON_NUMERIC_SPEECH_INPUT_ERROR = 991865;
    public static final int NEGATIVE_NUMBER_NOT_ALLOWED_INPUT_ERROR = 991866;
    @org.jetbrains.annotations.NotNull
    public static final org.dhis2.android.rtsm.commons.Constants INSTANCE = null;
    
    private Constants() {
        super();
    }
}
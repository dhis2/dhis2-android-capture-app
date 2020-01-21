package org.dhis2.utils;

/**
 * QUADRAM. Created by ppajuelo on 15/01/2018.
 */

public class Constants {

    public static final String EXTRA_DATA = "extra_data";

    //PREFERENCES
    public static final String SHARE_PREFS = "org.dhis2";
    public static final String PREFS_URLS = "pref_urls";
    public static final String PREFS_USERS = "pref_users";

    //SYNC PARAMETERS
    public static final String EVENT_MAX = "EVENT_MAX";
    public static final String TEI_MAX = "TEI_MAX";
    public static final String LIMIT_BY_ORG_UNIT = "LIMIT_BY_ORG_UNIT";
    public static final String LIMIT_BY_PROGRAM = "LIMIT_BY_PROGRAM";
    public static final int EVENT_MAX_DEFAULT = 1000;
    public static final int TEI_MAX_DEFAULT = 500;

    //RQ CODES
    public static final int RQ_QR_SCANNER = 101;

    // MAP SELECTION
    public static final int RQ_MAP_LOCATION = 102;
    public static final int RQ_MAP_LOCATION_VIEW = 103;
    //RQ_CODES
    public static final int RQ_ENROLLMENTS = 2001;
    public static final int REQ_ADD_RELATIONSHIP = 2002;

    // METADATA SYNC TIME RANGES
    public static final int TIME_DAILY = 86400;
    public static final int TIME_WEEKLY = 604800;
    public static final int TIME_MANUAL = 0;

    // DATA SYNC TIME RANGES
    public static final int TIME_15M = 15 * 60;
    public static final int TIME_HOURLY = 60 * 60;

    //PICTURE PICKER
    public static final int CAMERA_REQUEST = 108;
    public static final int GALLERY_REQUEST = 143;


    // LOGIN
    public static final String ACCOUNT_RECOVERY = "/dhis-web-commons/security/recovery.action";

    //EVENT_CREATION
    public static final String PREVIOUS_DASHBOARD_PROGRAM = "previous_dashboard_program";
    public static final String SCREEN_NAME = "SCREEN_NAME";
    public static final String PROGRAM_THEME = "PROGRAM_THEME";
    public static final String SERVER = "SERVER";
    public static final String THEME = "THEME";
    public static final String DATA_SET_UID = "DATA_SET_UID";
    public static final String DATA_SET_NAME = "DATA_SET_NAME";
    public static final int DESCRIPTION_DIALOG = 111;
    public static final String PERIOD_TYPE = "PERIOD_TYPE";
    public static final String PERIOD_ID = "PERIOD_ID";
    public static final String PERIOD_TYPE_DATE = "PERIOD_TYPE_DATE";
    public static final String CAT_COMB = "CAT_COMB";
    public static final String DEFAULT_CAT_COMBO = "DEFAULT_CAT_COMB";
    public static final String DATA_SET_SECTION = "DATA_SET_SECTION";
    public static final String PROGRAM_STAGE_UID = "PROGRAM_STAGE_UID";
    public static final int RQ_MANDATORY_EVENTS = 2001;
    public static final String OPTION_SET_DIALOG_THRESHOLD = "optionSetDialogThredshold";
    public static final String USER_TEST_ANDROID = "android";
    public static final String SECURE_SERVER_URL = "SEURE_SERVER_URL";
    public static final String SECURE_USER_NAME = "SECURE_USER_NAME";
    public static final String SECURE_PASS = "SECURE_PASS";
    public static final String SECURE_CREDENTIALS = "SECURE_CREDENTIALS";
    public static final String USER = "USER";
    public static final String USER_ASKED_CRASHLYTICS = "USER_ACCEPT_CRASHLYTICS";
    public static final String ENROLLMENT_STATUS = "ENROLLMENT_STATUS";

    public static final String LAST_DATA_SYNC = "last_data_sync";
    public static final String LAST_DATA_SYNC_STATUS = "last_data_sync_status";
    public static final String LAST_META_SYNC = "last_meta_sync";
    public static final String LAST_META_SYNC_STATUS = "last_meta_sync_status";
    public static final String LAST_META_SYNC_NO_NETWORK = "last_meta_sync_no_network";

    public static final String EVENT_UID = "EVENT_UID";
    public static final String EVENT_MODE = "EVENT_MODE";

    public static final String EVENT_CREATION_TYPE = "EVENT_CREATION_TYPE";
    public static final String EVENT_SCHEDULE_INTERVAL = "EVENT_SCHEDULE_INTERVAL";
    public static final String TRACKED_ENTITY_INSTANCE = "TRACKED_ENTITY_INSTANCE";
    public static final String PROGRAM_UID = "PROGRAM_UID";
    public static final String ORG_UNIT = "ORG_UNIT";
    public static final String ORG_UNIT_NAME = "ORG_UNIT_NAME";
    public static final String ONE_TIME = "ONE_TIME";
    public static final String PERMANENT = "PERMANENT";
    public static final String ENROLLMENT_UID = "ENROLLMENT_UID";
    public static final String EVENT_REPEATABLE = "EVENT_REPEATABLE";
    public static final String EVENT_PERIOD_TYPE = "EVENT_PERIOD_TYPE";
    public static final String ENROLLMENT_DATE_UID = "ENROLLMENT_DATE_UID";
    public static final String ENROLLMENT_DATE = "enrollmentDate";
    public static final String INCIDENT_DATE = "incidentDate";

    public static final String INITIAL_SYNC = "INITIAL_SYNC";
    public static final String META = "METADATA";
    public static final String META_NOW = "METADATA_NOW";
    public static final String DATA = "DATA";
    public static final String DATA_NOW = "DATA_NOW";
    public static final String TIME_META = "timeMeta";
    public static final String TIME_DATA = "timeData";
    public static final String JIRA_AUTH = "JIRA_AUTH";
    public static final String JIRA_USER = "JIRA_USER";

    public static final String ACCESS_DATA = "access_data";

    //Granular sync
    public final static String UID = "UID";
    public final static String CONFLICT_TYPE = "CONFLICT_TYPE";
    public final static String ATTRIBUTE_OPTION_COMBO = "ATTRIBUTE_OPTION_COMBO";
    public final static String CATEGORY_OPTION_COMBO = "category_option_combo";
    public static final String INIT_META = "SYNC_INIT_META";
    public static final String INIT_DATA = "SYNC_INIT_DATA";

    public static final String DATA_SET = "DataSets";
    public static final String DATASET_UID = "DATASET_UID";
    public static final String TRACKED_ENTITY_UID = "TRACKED_ENTITY_UID";

    public static final String RESERVED = "TAG_RV";

    private Constants() {
        // hide public constructor
    }
}

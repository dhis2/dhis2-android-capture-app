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
    public static final int EVENT_MAX_DEFAULT = 1000;
    public static final int TEI_MAX_DEFAULT = 500;

    //RQ CODES
    public static final int RQ_QR_SCANNER = 101;

    // MAP SELECTION
    public static final int RQ_MAP_LOCATION = 102;
    public static final int RQ_MAP_LOCATION_VIEW = 103;
    public static final int RQ_PROGRAM_STAGE = 104;

    //RQ_CODES
    public static final int RQ_ENROLLMENTS = 2001;
    public static final int REQ_ADD_RELATIONSHIP = 2002;

    // METADATA SYNC TIME RANGES
    public static final int TIME_DAILY = 86400;
    public static final int TIME_WEEKLY = 604800;
    public static final int TIME_MANUAL = 0;

    // DATA SYNC TIME RANGES
    public static final int TIME_15M = 900;
    public static final int TIME_HOURLY = 3600;


    //EVENT_CREATION
    public static final String DEFAULT_CAT_OPTION = "as6ygGvUGNg";
    public static final String DEFAULT_CAT_OPTION_COMBO = "bRowv6yZOF2";
    public static final String SCREEN_NAME = "SCREEN_NAME";
    public static final String PROGRAM_THEME = "PROGRAM_THEME";
    public static final String SERVER = "SERVER";
    public static final String THEME = "THEME";
    public static final String DATA_SET_UID = "DATA_SET_UID";
    public static final int DESCRIPTION_DIALOG = 111;
    public static final String PERIOD_TYPE = "PERIOD_TYPE";
    public static final String PERIOD_TYPE_DATE = "PERIOD_TYPE_DATE";
    public static final String CAT_COMB = "CAT_COMB";
    public static final String DATA_SET_SECTION = "DATA_SET_SECTION";

    public static String LAST_DATA_SYNC = "last_data_sync";
    public static String LAST_DATA_SYNC_STATUS = "last_data_sync_status";
    public static String LAST_META_SYNC = "last_meta_sync";
    public static String LAST_META_SYNC_STATUS = "last_meta_sync_status";

    public static final String EVENT_UID = "EVENT_UID";

    public static final String EVENT_CREATION_TYPE = "EVENT_CREATION_TYPE";
    public static final String TRACKED_ENTITY_INSTANCE = "TRACKED_ENTITY_INSTANCE";
    public static final String REFERRAL = "REFERRAL";
    public static final String ADDNEW = "ADDNEW";
    public static final String SCHEDULENEW = "SCHEDULENEW";
    public static final String PROGRAM_UID = "PROGRAM_UID";
    public static final String NEW_EVENT = "NEW_EVENT";
    public static final String ORG_UNIT = "ORG_UNIT";
    public static final String ONE_TIME = "ONE_TIME";
    public static final String PERMANENT = "PERMANENT";
    public static final String ENROLLMENT_UID = "ENROLLMENT_UID";
    public static final String EVENT_REPEATABLE = "EVENT_REPEATABLE";
    public static final String EVENT_PERIOD_TYPE = "EVENT_PERIOD_TYPE";
    public static final String ENROLLMENT_DATE_UID = "ENROLLMENT_DATE_UID";
    public static final String INCIDENT_DATE_UID = "INCIDENT_DATE_UID";


    private Constants() {
        // hide public constructor
    }
}

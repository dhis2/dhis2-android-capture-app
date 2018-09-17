package org.dhis2.utils;

/**
 * Created by ppajuelo on 15/01/2018.
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

    public static final String EVENT_UID = "EVENT_UID";

    //EVENT_CREATION
    public static final String DEFAULT_CAT_OPTION = "as6ygGvUGNg";
    public static final String DEFAULT_CAT_OPTION_COMBO = "bRowv6yZOF2";

    private Constants() {
        // hide public constructor
    }
}

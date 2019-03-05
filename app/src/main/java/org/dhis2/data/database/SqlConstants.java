package org.dhis2.data.database;

import org.hisp.dhis.android.core.program.ProgramRuleActionModel;
import org.hisp.dhis.android.core.program.ProgramRuleModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.resource.ResourceModel;
import org.hisp.dhis.android.core.settings.SystemSettingModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.user.AuthenticatedUserModel;
import org.hisp.dhis.android.core.user.UserModel;

public class SqlConstants {

    public static final String RESOURCE_TABLE = ResourceModel.TABLE;
    public static final String USER_TABLE = UserModel.TABLE;
    public static final String PROGRAM_RULE_ACTION_TABLE = ProgramRuleActionModel.TABLE;
    public static final String AUTH_USER_TABLE = AuthenticatedUserModel.TABLE;
    public static final String SYSTEM_SETTING_TABLE = SystemSettingModel.TABLE;


    public static final String TE_ATTR_TABLE = TrackedEntityAttributeModel.TABLE;
    public static final String TE_ATTR_UID = TrackedEntityAttributeModel.Columns.UID;
    public static final String TE_ATTR_VALUE_TYPE = TrackedEntityAttributeModel.Columns.VALUE_TYPE;
    public static final String TE_ATTR_OPTION_SET = TrackedEntityAttributeModel.Columns.OPTION_SET;
    public static final String TE_ATTR_DISPLAY_IN_LIST_NO_PROGRAM = TrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST_NO_PROGRAM;
    public static final String TE_ATTR_DISPLAY_NAME = TrackedEntityAttributeModel.Columns.DISPLAY_NAME;
    public static final String TE_ATTR_UNIQUE = TrackedEntityAttributeModel.Columns.UNIQUE;
    public static final String TE_ATTR_SORT_ORDER_IN_LIST_NO_PROGRAM = TrackedEntityAttributeModel.Columns.SORT_ORDER_IN_LIST_NO_PROGRAM;


    public static final String PROGRAM_TE_ATTR_TABLE = ProgramTrackedEntityAttributeModel.TABLE;
    public static final String PROGRAM_TE_ATTR_TRACKED_ENTITY_ATTRIBUTE = ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE;
    public static final String PROGRAM_TE_ATTR_PROGRAM = ProgramTrackedEntityAttributeModel.Columns.PROGRAM;
    public static final String PROGRAM_TE_ATTR_SORT_ORDER = ProgramTrackedEntityAttributeModel.Columns.SORT_ORDER;
    public static final String PROGRAM_TE_ATTR_SEARCHABLE = ProgramTrackedEntityAttributeModel.Columns.SEARCHABLE;
    public static final String PROGRAM_TE_ATTR_DISPLAY_IN_LIST = ProgramTrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST;


    public static final String PROGRAM_RULE_TABLE = ProgramRuleModel.TABLE;
    public static final String PROGRAM_RULE_UID = ProgramRuleModel.Columns.UID;
    public static final String PROGRAM_RULE_PROGRAM_STAGE = ProgramRuleModel.Columns.PROGRAM_STAGE;
    public static final String PROGRAM_RULE_PRIORITY = ProgramRuleModel.Columns.PRIORITY;
    public static final String PROGRAM_RULE_CONDITION = ProgramRuleModel.Columns.CONDITION;
    public static final String PROGRAM_RULE_PROGRAM = ProgramRuleModel.Columns.PROGRAM;

    public static final String PROGRAM_STAGE_TABLE = ProgramStageModel.TABLE;
    public static final String PROGRAM_STAGE_PROGRAM = ProgramStageModel.Columns.PROGRAM;
    public static final String PROGRAM_STAGE_UID = ProgramStageModel.Columns.UID;
    public static final String PROGRAM_STAGE_SORT_ORDER = ProgramStageModel.Columns.SORT_ORDER;
    public static final String PROGRAM_STAGE_DISPLAY_GENERATE_EVENT_BOX = ProgramStageModel.Columns.DISPLAY_GENERATE_EVENT_BOX;
    public static final String PROGRAM_STAGE_MIN_DAYS_FROM_START = ProgramStageModel.Columns.MIN_DAYS_FROM_START;
    public static final String PROGRAM_STAGE_REPORT_DATE_TO_USE = ProgramStageModel.Columns.REPORT_DATE_TO_USE;
    public static final String PROGRAM_STAGE_PERIOD_TYPE = ProgramStageModel.Columns.PERIOD_TYPE;
    public static final String PROGRAM_STAGE_AUTO_GENERATE_EVENT = ProgramStageModel.Columns.AUTO_GENERATE_EVENT;
    public static final String PROGRAM_STAGE_ACCESS_DATA_WRITE = ProgramStageModel.Columns.ACCESS_DATA_WRITE;
    public static final String PROGRAM_STAGE_DISPLAY_NAME = ProgramStageModel.Columns.DISPLAY_NAME;


    public static final String SELECT = "SELECT ";
    public static final String SELECT_DISTINCT = "SELECT DISTINCT ";
    public static final String FROM = " FROM ";
    public static final String WHERE = " WHERE ";
    public static final String WHEN = " WHEN ";
    public static final String THEN = " THEN ";
    public static final String ELSE = " ELSE ";
    public static final String END = " END ";
    public static final String LIMIT_1 = " LIMIT 1";
    public static final String LIMIT_10 = " LIMIT 10";
    public static final String DESC = " DESC ";
    public static final String ASC = " ASC ";
    public static final String JOIN = " JOIN ";
    public static final String INNER_JOIN = " INNER JOIN ";
    public static final String QUOTE = "'";
    public static final String ON = " ON ";
    public static final String IN = " IN ";
    public static final String POINT = ".";
    public static final String EQUAL = " = ";
    public static final String NOT_EQUAL = " != ";
    public static final String IS_NOT_NULL = " IS NOT NULL ";
    public static final String IS_NULL = " IS NULL ";
    public static final String LESS_OR_EQUAL = " <= ";
    public static final String LESS_THAN = " < ";
    public static final String GREAT_THAN = " > ";
    public static final String ALL = "*";
    public static final String QUESTION_MARK = "?";
    public static final String COMMA = ", ";
    public static final String AND = " AND ";
    public static final String OR = " OR ";
    public static final String TABLE_POINT_FIELD_EQUALS = "%s.%s = ";
    public static final String TABLE_POINT_FIELD_NOT_EQUALS = "%s.%s != ";
    public static final String ORDER_BY = " ORDER BY ";
    public static final String ORDER_BY_CASE = " ORDER BY CASE ";
    public static final String GROUP_BY = " GROUP BY ";
    public static final String AS = " AS ";
    public static final String LEFT_OUTER_JOIN = " LEFT OUTER JOIN ";
    public static final String VARIABLE = "%s";
    public static final String TABLE_POINT_FIELD = "%s.%s";
    public static final String JOIN_VARIABLE_ON_TABLE_POINT_FIELD_EQUALS = " JOIN %s ON %s.%s = %s.%s";

    private SqlConstants() {
        // hide public constructor
    }
}

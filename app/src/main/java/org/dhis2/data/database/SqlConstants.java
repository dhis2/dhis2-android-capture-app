package org.dhis2.data.database;

import org.hisp.dhis.android.core.category.CategoryOptionComboCategoryOptionLinkModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.datavalue.DataValueModel;
import org.hisp.dhis.android.core.legendset.ProgramIndicatorLegendSetLinkModel;
import org.hisp.dhis.android.core.period.PeriodModel;
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


//    PROGRAM INDICATOR LEGEND SET LINK
    public static final String PROGRAM_INDICATOR_LEGEND_SET_LINK_TABLE = ProgramIndicatorLegendSetLinkModel.TABLE;
    public static final String PROGRAM_INDICATOR_LEGEND_SET_LINK_LEGEND_SET = ProgramIndicatorLegendSetLinkModel.Columns.LEGEND_SET;
    public static final String PROGRAM_INDICATOR_LEGEND_SET_LINK_PROGRAM_INDICATOR = ProgramIndicatorLegendSetLinkModel.Columns.PROGRAM_INDICATOR;



//    CATEGORY OPTION COMBO CATEGORY OPTION LINK
    public static final String CAT_OPTION_COMBO_CAT_OPTION_LINK_TABLE = CategoryOptionComboCategoryOptionLinkModel.TABLE;
    public static final String CAT_OPTION_COMBO_CAT_OPTION_LINK_CATEGORY_OPTION = CategoryOptionComboCategoryOptionLinkModel.Columns.CATEGORY_OPTION;
    public static final String CAT_OPTION_COMBO_CAT_OPTION_LINK_CATEGORY_OPTION_COMBO = CategoryOptionComboCategoryOptionLinkModel.Columns.CATEGORY_OPTION_COMBO;


    //    PERIOD
    public static final String PERIOD_TABLE = PeriodModel.TABLE;
    public static final String PERIOD_PERIOD_ID = PeriodModel.Columns.PERIOD_ID;

    //    DATA VALUE
    public static final String DATA_VALUE_TABLE = DataValueModel.TABLE;
    public static final String DATA_VALUE_OU = DataValueModel.Columns.ORGANISATION_UNIT;
    public static final String DATA_VALUE_PERIOD = DataValueModel.Columns.PERIOD;
    public static final String DATA_VALUE_ATTRIBUTE_OPTION_COMBO = DataValueModel.Columns.ATTRIBUTE_OPTION_COMBO;
    public static final String DATA_VALUE_DATA_ELEMENT = DataValueModel.Columns.DATA_ELEMENT;
    public static final String DATA_VALUE_CATEGORY_OPTION_COMBO = DataValueModel.Columns.CATEGORY_OPTION_COMBO;
    public static final String DATA_VALUE_STATE = DataValueModel.Columns.STATE;


    //    DATA ELEMENT
    public static final String DATA_ELEMENT_TABLE = DataElementModel.TABLE;
    public static final String DATA_ELEMENT_UID = DataElementModel.Columns.UID;
    public static final String DATA_ELEMENT_FORM_NAME = DataElementModel.Columns.FORM_NAME;


    //    RESOURCE
    public static final String RESOURCE_TABLE = ResourceModel.TABLE;
    public static final String USER_TABLE = UserModel.TABLE;
    public static final String PROGRAM_RULE_ACTION_TABLE = ProgramRuleActionModel.TABLE;
    public static final String AUTH_USER_TABLE = AuthenticatedUserModel.TABLE;
    public static final String SYSTEM_SETTING_TABLE = SystemSettingModel.TABLE;


    //    TRACKED ENTITY ATTRIBUTE
    public static final String TE_ATTR_TABLE = TrackedEntityAttributeModel.TABLE;
    public static final String TE_ATTR_UID = TrackedEntityAttributeModel.Columns.UID;
    public static final String TE_ATTR_VALUE_TYPE = TrackedEntityAttributeModel.Columns.VALUE_TYPE;
    public static final String TE_ATTR_OPTION_SET = TrackedEntityAttributeModel.Columns.OPTION_SET;
    public static final String TE_ATTR_DISPLAY_IN_LIST_NO_PROGRAM = TrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST_NO_PROGRAM;
    public static final String TE_ATTR_DISPLAY_NAME = TrackedEntityAttributeModel.Columns.DISPLAY_NAME;
    public static final String TE_ATTR_UNIQUE = TrackedEntityAttributeModel.Columns.UNIQUE;
    public static final String TE_ATTR_SORT_ORDER_IN_LIST_NO_PROGRAM = TrackedEntityAttributeModel.Columns.SORT_ORDER_IN_LIST_NO_PROGRAM;


    //    PROGRAM TRACKED ENTITY ATTRIBUTE
    public static final String PROGRAM_TE_ATTR_TABLE = ProgramTrackedEntityAttributeModel.TABLE;
    public static final String PROGRAM_TE_ATTR_TRACKED_ENTITY_ATTRIBUTE = ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE;
    public static final String PROGRAM_TE_ATTR_PROGRAM = ProgramTrackedEntityAttributeModel.Columns.PROGRAM;
    public static final String PROGRAM_TE_ATTR_SORT_ORDER = ProgramTrackedEntityAttributeModel.Columns.SORT_ORDER;
    public static final String PROGRAM_TE_ATTR_SEARCHABLE = ProgramTrackedEntityAttributeModel.Columns.SEARCHABLE;
    public static final String PROGRAM_TE_ATTR_DISPLAY_IN_LIST = ProgramTrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST;


    //    PROGRAM RULE
    public static final String PROGRAM_RULE_TABLE = ProgramRuleModel.TABLE;
    public static final String PROGRAM_RULE_UID = ProgramRuleModel.Columns.UID;
    public static final String PROGRAM_RULE_PROGRAM_STAGE = ProgramRuleModel.Columns.PROGRAM_STAGE;
    public static final String PROGRAM_RULE_PRIORITY = ProgramRuleModel.Columns.PRIORITY;
    public static final String PROGRAM_RULE_CONDITION = ProgramRuleModel.Columns.CONDITION;
    public static final String PROGRAM_RULE_PROGRAM = ProgramRuleModel.Columns.PROGRAM;


    //    PROGRAM STAGE
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

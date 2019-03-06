package org.dhis2.data.database;

@SuppressWarnings("squid:CommentedOutCodeLine")
public class SqlConstants {

    //    PROGRAM INDICATOR LEGEND SET LINK
    public static final String PROGRAM_INDICATOR_LEGEND_SET_LINK_TABLE = "ProgramIndicatorLegendSetLink"; //ProgramIndicatorLegendSetLinkModel.TABLE;
    public static final String PROGRAM_INDICATOR_LEGEND_SET_LINK_LEGEND_SET = "legendSet"; //ProgramIndicatorLegendSetLinkModel.Columns.LEGEND_SET;
    public static final String PROGRAM_INDICATOR_LEGEND_SET_LINK_PROGRAM_INDICATOR = "programIndicator"; // ProgramIndicatorLegendSetLinkModel.Columns.PROGRAM_INDICATOR;


    //    CATEGORY OPTION COMBO CATEGORY OPTION LINK
    public static final String CAT_OPTION_COMBO_CAT_OPTION_LINK_TABLE = "CategoryOptionComboCategoryOptionLink"; //CategoryOptionComboCategoryOptionLinkModel.TABLE;
    public static final String CAT_OPTION_COMBO_CAT_OPTION_LINK_CATEGORY_OPTION = "categoryOption"; //CategoryOptionComboCategoryOptionLinkModel.Columns.CATEGORY_OPTION;
    public static final String CAT_OPTION_COMBO_CAT_OPTION_LINK_CATEGORY_OPTION_COMBO = "categoryOptionCombo"; //CategoryOptionComboCategoryOptionLinkModel.Columns.CATEGORY_OPTION_COMBO;


    //    PERIOD
    public static final String PERIOD_TABLE = "Period"; //PeriodModel.TABLE;
    public static final String PERIOD_PERIOD_ID = "periodId"; //PeriodModel.Columns.PERIOD_ID;

    //    DATA VALUE
    public static final String DATA_VALUE_TABLE = "DataValue"; //DataValueModel.TABLE;
    public static final String DATA_VALUE_OU = "organisationUnit"; //DataValueModel.Columns.ORGANISATION_UNIT;
    public static final String DATA_VALUE_PERIOD = "period"; //DataValueModel.Columns.PERIOD;
    public static final String DATA_VALUE_ATTRIBUTE_OPTION_COMBO = "attributeOptionCombo"; //DataValueModel.Columns.ATTRIBUTE_OPTION_COMBO;
    public static final String DATA_VALUE_DATA_ELEMENT = "dataElement"; //DataValueModel.Columns.DATA_ELEMENT;
    public static final String DATA_VALUE_CATEGORY_OPTION_COMBO = "categoryOptionCombo"; //DataValueModel.Columns.CATEGORY_OPTION_COMBO;
    public static final String DATA_VALUE_STATE = "state"; //DataValueModel.Columns.STATE;


    //    DATA ELEMENT
    public static final String DATA_ELEMENT_TABLE = "DataElement"; //DataElementModel.TABLE;
    public static final String DATA_ELEMENT_UID = "uid"; //DataElementModel.Columns.UID;
    public static final String DATA_ELEMENT_FORM_NAME = "formName"; //DataElementModel.Columns.FORM_NAME;


    public static final String RESOURCE_TABLE = "Resource"; //ResourceModel.TABLE;
    public static final String USER_TABLE = "User"; //UserModel.TABLE;
    public static final String PROGRAM_RULE_ACTION_TABLE = "ProgramRuleAction"; //ProgramRuleActionModel.TABLE;
    public static final String AUTH_USER_TABLE = "AuthenticatedUser"; //AuthenticatedUserModel.TABLE;
    public static final String SYSTEM_SETTING_TABLE = "SystemSetting"; //SystemSettingModel.TABLE;


    //    TRACKED ENTITY ATTRIBUTE
    public static final String TE_ATTR_TABLE = "TrackedEntityAttribute"; //TrackedEntityAttributeModel.TABLE;
    public static final String TE_ATTR_UID = "uid"; //TrackedEntityAttributeModel.Columns.UID;
    public static final String TE_ATTR_VALUE_TYPE = "valueType"; //TrackedEntityAttributeModel.Columns.VALUE_TYPE;
    public static final String TE_ATTR_OPTION_SET = "optionSet"; //TrackedEntityAttributeModel.Columns.OPTION_SET;
    public static final String TE_ATTR_DISPLAY_IN_LIST_NO_PROGRAM = "displayInListNoProgram"; //TrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST_NO_PROGRAM;
    public static final String TE_ATTR_DISPLAY_NAME = "displayName"; //TrackedEntityAttributeModel.Columns.DISPLAY_NAME;
    public static final String TE_ATTR_UNIQUE = "uniqueProperty"; //TrackedEntityAttributeModel.Columns.UNIQUE;
    public static final String TE_ATTR_SORT_ORDER_IN_LIST_NO_PROGRAM = "sortOrderInListNoProgram"; //TrackedEntityAttributeModel.Columns.SORT_ORDER_IN_LIST_NO_PROGRAM;


    //    PROGRAM TRACKED ENTITY ATTRIBUTE
    public static final String PROGRAM_TE_ATTR_TABLE = "ProgramTrackedEntityAttribute"; //ProgramTrackedEntityAttributeModel.TABLE;
    public static final String PROGRAM_TE_ATTR_TRACKED_ENTITY_ATTRIBUTE = "trackedEntityAttribute"; //ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE;
    public static final String PROGRAM_TE_ATTR_PROGRAM = "program"; //ProgramTrackedEntityAttributeModel.Columns.PROGRAM;
    public static final String PROGRAM_TE_ATTR_SORT_ORDER = "sortOrder"; //ProgramTrackedEntityAttributeModel.Columns.SORT_ORDER;
    public static final String PROGRAM_TE_ATTR_SEARCHABLE = "searchable"; //ProgramTrackedEntityAttributeModel.Columns.SEARCHABLE;
    public static final String PROGRAM_TE_ATTR_DISPLAY_IN_LIST = "displayInList"; //ProgramTrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST;


    //    PROGRAM RULE
    public static final String PROGRAM_RULE_TABLE = "ProgramRule"; //ProgramRuleModel.TABLE;
    public static final String PROGRAM_RULE_UID = "uid"; //ProgramRuleModel.Columns.UID;
    public static final String PROGRAM_RULE_PROGRAM_STAGE = "programStage"; //ProgramRuleModel.Columns.PROGRAM_STAGE;
    public static final String PROGRAM_RULE_PRIORITY = "priority"; //ProgramRuleModel.Columns.PRIORITY;
    public static final String PROGRAM_RULE_CONDITION = "condition"; //ProgramRuleModel.Columns.CONDITION;
    public static final String PROGRAM_RULE_PROGRAM = "program"; //ProgramRuleModel.Columns.PROGRAM;


    //    PROGRAM STAGE
    public static final String PROGRAM_STAGE_TABLE = "ProgramStage"; //ProgramStageModel.TABLE;
    public static final String PROGRAM_STAGE_PROGRAM = "program"; //ProgramStageModel.Columns.PROGRAM;
    public static final String PROGRAM_STAGE_UID = "uid"; //ProgramStageModel.Columns.UID;
    public static final String PROGRAM_STAGE_SORT_ORDER = "sortOrder"; //ProgramStageModel.Columns.SORT_ORDER;
    public static final String PROGRAM_STAGE_DISPLAY_GENERATE_EVENT_BOX = "displayGenerateEventBox"; //ProgramStageModel.Columns.DISPLAY_GENERATE_EVENT_BOX;
    public static final String PROGRAM_STAGE_MIN_DAYS_FROM_START = "minDaysFromStart"; //ProgramStageModel.Columns.MIN_DAYS_FROM_START;
    public static final String PROGRAM_STAGE_REPORT_DATE_TO_USE = "reportDateToUse"; //ProgramStageModel.Columns.REPORT_DATE_TO_USE;
    public static final String PROGRAM_STAGE_PERIOD_TYPE = "periodType"; //ProgramStageModel.Columns.PERIOD_TYPE;
    public static final String PROGRAM_STAGE_AUTO_GENERATE_EVENT = "autoGenerateEvent"; //ProgramStageModel.Columns.AUTO_GENERATE_EVENT;
    public static final String PROGRAM_STAGE_ACCESS_DATA_WRITE = "accessDataWrite"; //ProgramStageModel.Columns.ACCESS_DATA_WRITE;
    public static final String PROGRAM_STAGE_DISPLAY_NAME = "displayName"; //ProgramStageModel.Columns.DISPLAY_NAME;


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

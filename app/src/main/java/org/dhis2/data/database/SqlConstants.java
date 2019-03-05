package org.dhis2.data.database;

import org.hisp.dhis.android.core.program.ProgramStageModel;

public class SqlConstants {

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

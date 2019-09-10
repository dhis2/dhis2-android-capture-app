package org.dhis2.usescases.datasets.datasetInitial;

import android.database.Cursor;

import androidx.annotation.Nullable;

import com.gabrielittner.auto.value.cursor.ColumnAdapter;
import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.arch.db.adapters.custom.internal.DbDateColumnAdapter;
import org.hisp.dhis.android.core.common.BaseModel;

import java.util.Date;

@AutoValue
public abstract class DateRangeInputPeriodModel {

    public static class Columns extends BaseModel.Columns {
        public static final String DATA_SET = "dataSet";
        public static final String PERIOD = "period";
        public static final String OPENING_DATE = "openingDate";
        public static final String CLOSING_DATE = "closingDate";
        public static final String INITIAL_PERIOD = "initialPeriodDate";
        public static final String END_PERIOD = "endPeriodDate";
    }

    public static DateRangeInputPeriodModel fromCursor(Cursor cursor) {
        return AutoValue_DateRangeInputPeriodModel.createFromCursor(cursor);
    }

    public static DateRangeInputPeriodModel create(String dataSet, String period, Date openingDate, Date closingDate, Date initialPeriodDate, Date endPeriodDate) {
        return new AutoValue_DateRangeInputPeriodModel(dataSet, period, openingDate, closingDate, initialPeriodDate, endPeriodDate);
    }

    public final static String TABLE = "DataInputPeriod";

    @ColumnName(Columns.DATA_SET)
    public abstract String dataSet();

    @ColumnName(Columns.PERIOD)
    public abstract String period();

    @Nullable
    @ColumnName(Columns.OPENING_DATE)
    @ColumnAdapter(DbDateColumnAdapter.class)
    public abstract Date openingDate();

    @Nullable
    @ColumnName(Columns.CLOSING_DATE)
    @ColumnAdapter(DbDateColumnAdapter.class)
    public abstract Date closingDate();

    @Nullable
    @ColumnName(Columns.INITIAL_PERIOD)
    @ColumnAdapter(DbDateColumnAdapter.class)
    public abstract Date initialPeriodDate();

    @Nullable
    @ColumnName(Columns.END_PERIOD)
    @ColumnAdapter(DbDateColumnAdapter.class)
    public abstract Date endPeriodDate();

}

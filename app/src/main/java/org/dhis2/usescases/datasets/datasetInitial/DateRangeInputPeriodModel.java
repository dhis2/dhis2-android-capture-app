package org.dhis2.usescases.datasets.datasetInitial;

import java.util.Date;

import org.hisp.dhis.android.core.common.CoreColumns;

import com.google.auto.value.AutoValue;

import androidx.annotation.Nullable;

@AutoValue
public abstract class DateRangeInputPeriodModel {
    public static DateRangeInputPeriodModel create(String dataSet, String period, Date openingDate, Date closingDate, Date initialPeriodDate, Date endPeriodDate) {
        return new AutoValue_DateRangeInputPeriodModel(dataSet, period, openingDate, closingDate, initialPeriodDate, endPeriodDate);
    }

    public abstract String dataSet();

    public abstract String period();

    @Nullable
    public abstract Date openingDate();

    @Nullable
    public abstract Date closingDate();

    @Nullable
    public abstract Date initialPeriodDate();

    @Nullable
    public abstract Date endPeriodDate();

}

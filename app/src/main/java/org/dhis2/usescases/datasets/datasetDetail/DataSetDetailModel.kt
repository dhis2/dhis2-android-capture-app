package org.dhis2.usescases.datasets.datasetDetail;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.common.State;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

@AutoValue
public abstract class DataSetDetailModel {


    @NonNull
    public abstract String datasetUid();

    @NonNull
    public abstract String orgUnitUid();

    @NonNull
    public abstract String catOptionComboUid();

    @NonNull
    public abstract String periodId();

    @NonNull
    public abstract String nameOrgUnit();

    @NonNull
    public abstract String nameCatCombo();

    @NonNull
    public abstract String namePeriod();

    @NonNull
    public abstract State state();

    @NonNull
    public abstract String periodType();

    @NonNull
    public abstract Boolean displayOrgUnitName();

    @NonNull
    public abstract Boolean isComplete();

    @NotNull
    public abstract Date lastUpdated();

    @NotNull
    public abstract String nameCategoryOptionCombo();

    @NonNull
    public static DataSetDetailModel create(@NonNull String datasetUid, @NonNull String orgUnitUid, @NonNull String catOptionComboUid, @NonNull String periodId, @NonNull String orgUnitName, String nameCatCombo, String namePeriod, State state, String periodType, Boolean displayOrgUnitName, Boolean isComplete, Date lastUpdated, String nameCategoryOptionCombo) {
        return new AutoValue_DataSetDetailModel(datasetUid, orgUnitUid, catOptionComboUid, periodId, orgUnitName, nameCatCombo, namePeriod, state, periodType, displayOrgUnitName, isComplete, lastUpdated, nameCategoryOptionCombo);
    }
}

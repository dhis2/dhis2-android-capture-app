package org.dhis2.usescases.datasets.datasetDetail;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.common.State;

@AutoValue
public abstract class DataSetDetailModel {

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
    public static DataSetDetailModel create(@NonNull String orgUnitUid, @NonNull String catOptionComboUid, @NonNull String periodId, @NonNull String orgUnitName, String nameCatCombo, String namePeriod, State state, String periodType) {
        return new AutoValue_DataSetDetailModel(orgUnitUid, catOptionComboUid, periodId, orgUnitName, nameCatCombo, namePeriod, state, periodType);
    }

}

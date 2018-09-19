package org.dhis2.usescases.datasets.datasetInitial;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

public class DataSetInitialRepositoryImpl implements DataSetInitialRepository {
    @NonNull
    @Override
    public Observable<DataSetModel> dataSet(String datasetId) {
        return null;
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits(String programId) {
        return null;
    }

    @NonNull
    @Override
    public Observable<List<CategoryOptionComboModel>> catCombo(String programUid) {
        return null;
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> filteredOrgUnits(String date, String programId) {
        return null;
    }

    @Override
    public Observable<String> createDataSet(String enrollmentUid, @NonNull Context context, @NonNull Date date, @NonNull String orgUnitUid, @NonNull String catComboUid) {
        return null;
    }

    @Override
    public Observable<String> scheduleDataSet(String enrollmentUid, @Nullable String trackedEntityInstanceUid, @NonNull Context context, @NonNull String program, @NonNull String programStage, @NonNull Date dueDate, @NonNull String orgUnitUid, @NonNull String catComboUid, @NonNull String catOptionUid, @NonNull String latitude, @NonNull String longitude) {
        return null;
    }

    @Override
    public Observable<String> updateDataSetInstance(String eventId, String trackedEntityInstanceUid, String orgUnitUid) {
        return null;
    }

    @NonNull
    @Override
    public Observable<EventModel> newlyCreatedDataSet(long rowId) {
        return null;
    }

    @NonNull
    @Override
    public Observable<EventModel> editDataSet(String eventUid, String date, String orgUnitUid, String catComboUid, String catOptionCombo, String latitude, String longitude) {
        return null;
    }

    @Override
    public Observable<Boolean> accessDataWrite(String programId) {
        return null;
    }
}

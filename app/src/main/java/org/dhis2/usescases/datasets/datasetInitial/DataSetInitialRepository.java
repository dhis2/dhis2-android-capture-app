package org.dhis2.usescases.datasets.datasetInitial;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

public interface DataSetInitialRepository {

    @NonNull
    Observable<DataSetModel> dataSet(String datasetId);

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits(String programId);

    @NonNull
    Observable<List<CategoryOptionComboModel>> catCombo(String programUid);

    @NonNull
    Observable<List<OrganisationUnitModel>> filteredOrgUnits(String date, String programId);

    Observable<String> createDataSet(String enrollmentUid, @NonNull Context context, @NonNull Date date,
                                     @NonNull String orgUnitUid, @NonNull String catComboUid);
    //Saber si esto sirve
    Observable<String> scheduleDataSet(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                       @NonNull Context context, @NonNull String program,
                                       @NonNull String programStage, @NonNull Date dueDate,
                                       @NonNull String orgUnitUid, @NonNull String catComboUid,
                                       @NonNull String catOptionUid, @NonNull String latitude, @NonNull String longitude);

    //Update DataSet ¿?¿?¿
    Observable<String> updateDataSetInstance(String eventId, String trackedEntityInstanceUid, String orgUnitUid);

    @NonNull
    Observable<EventModel> newlyCreatedDataSet(long rowId);

    @NonNull
    Observable<EventModel> editDataSet(String eventUid, String date, String orgUnitUid, String catComboUid, String catOptionCombo, String latitude, String longitude);
    //¿Sobra???
    Observable<Boolean> accessDataWrite(String programId);
}

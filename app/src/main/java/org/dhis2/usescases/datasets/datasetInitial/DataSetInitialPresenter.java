package org.dhis2.usescases.datasets.datasetInitial;

import android.app.DatePickerDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dhis2.data.metadata.MetadataRepository;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DataSetInitialPresenter implements DataSetInitialContract.Presenter {

    private String datasetId;
    private CompositeDisposable compositeDisposable;
    private final MetadataRepository metadataRepository;
    private DataSetInitialRepository dataSetInitialRepository;

    public DataSetInitialPresenter(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Override
    public void init(DataSetInitialContract.View view, String datasetId, String orgUnitId) {
        this.datasetId = datasetId;
        compositeDisposable = new CompositeDisposable();

    }

    @Override
    public void onBackClick() {

    }

    @Override
    public void createDataSet(String enrollmentUid, String programStageModel, Date date, String orgUnitUid, String catOption, String catOptionCombo, String latitude, String longitude) {

    }

    @Override
    public void createDataSetPermanent(String enrollmentUid, String trackedEntityInstanceUid, String programStageModel, Date date, String orgUnitUid, String catOption, String catOptionCombo, String latitude, String longitude) {

    }

    @Override
    public void scheduleDataSet(String enrollmentUid, String programStageModel, Date dueDate, String orgUnitUid, String catOption, String catOptionCombo, String latitude, String longitude) {

    }

    @Override
    public void editDataSet(String programStageModel, String eventUid, String date, String orgUnitUid, String catOption, String catOptionCombo, String latitude, String longitude) {

    }

    @Override
    public void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener) {

    }

    @Override
    public void onOrgUnitButtonClick() {

    }

    @Override
    public void onLocationClick() {

    }

    @Override
    public void onLocation2Click() {

    }

    @Override
    public void getCatOption(String categoryOptionComboId) {

    }

    @Override
    public void filterOrgUnits(String date) {

    }

    @Override
    public void getSectionCompletion(@Nullable String sectionUid) {

    }

    @Override
    public void goToSummary() {

    }

    @Override
    public void getDataSet(String programUid, String enrollmentUid, String programStageUid, PeriodType periodType) {

    }

    @Override
    public void getOrgUnits(String programId) {

    }

    @Override
    public void getDataSetSections(@NonNull String eventId) {

    }

    @Override
    public List<OrganisationUnitModel> getOrgUnits() {
        return null;
    }

    @Override
    public void onDettach() {

    }

    @Override
    public void displayMessage(String message) {

    }
}

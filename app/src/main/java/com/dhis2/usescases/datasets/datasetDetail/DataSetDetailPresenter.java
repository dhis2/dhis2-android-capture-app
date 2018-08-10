package com.dhis2.usescases.datasets.datasetDetail;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.IntDef;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailContract;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailInteractor;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.OrgUnitUtils;
import com.dhis2.utils.Period;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.EVENT_UID;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.NEW_EVENT;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.ORG_UNIT;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.PROGRAM_UID;

public class DataSetDetailPresenter implements DataSetDetailContract.Presenter {

    private DataSetDetailRepository dataSetDetailRepository;
    private DataSetDetailContract.View view;
    private String programId;
    private ProgramModel program;
    private CategoryOptionComboModel categoryOptionComboModel;
    private MetadataRepository metadataRepository;
    private int lastSearchType;
    private Date fromDate;
    private Date toDate;
    private Period period;
    private List<Date> dates;
    private CompositeDisposable compositeDisposable;
    private List<OrganisationUnitModel> orgUnits;
    private CategoryComboModel mCatCombo;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LastSearchType.DATES, LastSearchType.DATE_RANGES})
    public @interface LastSearchType {
        int DATES = 1;
        int DATE_RANGES = 32;
    }

    public DataSetDetailPresenter(DataSetDetailRepository dataSetDetailRepository, MetadataRepository metadataRepository) {
        this.dataSetDetailRepository = dataSetDetailRepository;
        this.metadataRepository = metadataRepository;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(DataSetDetailContract.View view, String programId, Period period) {
        this.view = view;
        //FIXME creo que hay que quitarlo, los dataset creo que no tienen programs
        this.programId = programId;

        getOrgUnits(null);
    }

    @Override
    public void onTimeButtonClick() {
        view.showTimeUnitPicker();
    }

    @Override
    public void onDateRangeButtonClick() {
        view.showRageDatePicker();
    }

    @Override
    public void onOrgUnitButtonClick() {
        view.openDrawer();
    }

    @Override
    public ProgramModel getCurrentProgram() {
        return program;
    }

    @Override
    public void addDataSet() {
        //FIXME cambiar por la actividad de un datasetinitialactiviy
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, programId);
        bundle.putBoolean(NEW_EVENT, true);

        view.startActivity(EventInitialActivity.class, bundle, false, false, null);
    }

    @Override
    public void onBackClick() {
        if (view != null)
            view.back();
    }

    @Override
    public void onCatComboSelected(CategoryOptionComboModel categoryOptionComboModel, String orgUnitQuery) {
        updateFilters(categoryOptionComboModel, orgUnitQuery);
    }

    @Override
    public void clearCatComboFilters(String orgUnitQuery) {
        updateFilters(null, orgUnitQuery);
    }

    @Override
    public void onDataSetClick(String eventId, String orgUnit) {
        //FIXME tambien cambiar por la DataSetInitialActivity
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, programId);
        bundle.putString(EVENT_UID, eventId);
        bundle.putString(ORG_UNIT, orgUnit);
        bundle.putBoolean(NEW_EVENT, false);
        view.startActivity(EventInitialActivity.class, bundle, false, false, null);
    }

    @Override
    public List<OrganisationUnitModel> getOrgUnits() {
        return this.orgUnits;
    }

    @Override
    public Observable<List<TrackedEntityDataValueModel>> getDataSetDataValue(DataSetModel dataSet) {
        return dataSetDetailRepository.dataSetDataValues(dataSet);
    }

    @Override
    public Observable<List<String>> getDataSetDataValueNew(DataSetModel dataSet) {
        return dataSetDetailRepository.dataSetValuesNew(dataSet);
    }

    @Override
    public void showFilter() {
        view.showHideFilter();
    }

    @SuppressLint("CheckResult")
    @Override
    public void getDataSets(Date fromDate, Date toDate, String orgUnitQuery) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        lastSearchType = ProgramEventDetailInteractor.LastSearchType.DATES;
        Observable.just(dataSetDetailRepository.filteredDataSet(
                DateUtils.getInstance().formatDate(fromDate),
                DateUtils.getInstance().formatDate(toDate),
                categoryOptionComboModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setData,
                        Timber::e));
    }

    @Override
    public void getOrgUnits(Date date) {
        compositeDisposable.add(dataSetDetailRepository.orgUnits()
                .map(orgUnits -> {
                    this.orgUnits = orgUnits;
                    return OrgUnitUtils.renderTree(view.getContext(), orgUnits, true);
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        treeNode -> view.addTree(treeNode),
                        throwable -> view.renderError(throwable.getMessage())
                ));
    }

    private void updateFilters(CategoryOptionComboModel categoryOptionComboModel, String orgUnitQuery) {
        this.categoryOptionComboModel = categoryOptionComboModel;
        switch (lastSearchType) {
            case DataSetDetailPresenter.LastSearchType.DATES:
                getDataSets( this.fromDate, this.toDate, orgUnitQuery);
                break;
            case DataSetDetailPresenter.LastSearchType.DATE_RANGES:
                getDataSetWithDates(this.dates, this.period, orgUnitQuery);
                break;
            default:
                getDataSetWithDates(null, this.period, orgUnitQuery);
                break;
        }
    }

    @Override
    public void getDataSetWithDates(List<Date> dates, Period period, String orgUnitQuery) {
        this.dates = dates;
        this.period = period;
        lastSearchType = ProgramEventDetailInteractor.LastSearchType.DATE_RANGES;
        //FIXME cuando haya datos para dataset hay que cambiarlo
        //ahora falla por que se va a hacer la select y no puede
       /* compositeDisposable.add(dataSetDetailRepository.filteredDataSet(dates, period, categoryOptionComboModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setData,
                        throwable -> view.renderError(throwable.getMessage())));*/
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    private void getCatCombo(ProgramModel programModel) {
        compositeDisposable.add(metadataRepository.getCategoryComboWithId(programModel.categoryCombo())
                .filter(categoryComboModel -> categoryComboModel != null && !categoryComboModel.uid().equals(CategoryComboModel.DEFAULT_UID))
                .flatMap(catCombo -> {
                    mCatCombo = catCombo;
                    return dataSetDetailRepository.catCombo(programModel.categoryCombo());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(catComboOptions -> view.setCatComboOptions(mCatCombo, catComboOptions), Timber::d)
        );
    }
}

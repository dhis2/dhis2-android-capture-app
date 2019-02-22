package org.dhis2.usescases.datasets.datasetDetail;

import android.annotation.SuppressLint;
import android.os.Bundle;

import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.OrgUnitUtils;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;

import androidx.annotation.IntDef;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class DataSetDetailPresenterImpl implements DataSetDetailContract.DataSetDetailPresenter {

    private DataSetDetailRepository dataSetDetailRepository;
    private DataSetDetailContract.DataSetDetailView view;
    private int lastSearchType;
    private Date fromDate;
    private Date toDate;
    private Period period;
    private List<Date> dates;
    private CompositeDisposable compositeDisposable;
    private List<OrganisationUnitModel> orgUnits;
    private List<String> selectedOrgUnits;
    private PeriodType selectedPeriodType;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LastSearchType.DATES, LastSearchType.DATE_RANGES})
    public @interface LastSearchType {
        int DATES = 1;
        int DATE_RANGES = 32;
    }

    public DataSetDetailPresenterImpl(DataSetDetailRepository dataSetDetailRepository) {
        this.dataSetDetailRepository = dataSetDetailRepository;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(DataSetDetailContract.DataSetDetailView view) {
        this.view = view;
        getOrgUnits(null);
        compositeDisposable.add(
                view.dataSetPage()
                        .startWith(0)
                        .flatMap(page -> dataSetDetailRepository.dataSetGroups(view.dataSetUid(), selectedOrgUnits, selectedPeriodType, page))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::setData,
                                Timber::d
                        )
        );
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
    public void addDataSet() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_SET_UID, view.dataSetUid());

        view.startActivity(DataSetInitialActivity.class, bundle, false, false, null);
    }

    @Override
    public void onBackClick() {
        if (view != null)
            view.back();
    }

    @Override
    public void onCatComboSelected(CategoryOptionComboModel categoryOptionComboModel, String orgUnitQuery) {
        updateFilters(orgUnitQuery);
    }

    @Override
    public void clearCatComboFilters(String orgUnitQuery) {
        updateFilters(orgUnitQuery);
    }

    @Override
    public void onDataSetClick(String eventId, String orgUnit) {
        // do nothing
    }

    @Override
    public List<OrganisationUnitModel> getOrgUnits() {
        return this.orgUnits;
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
        lastSearchType = LastSearchType.DATES;
    }

    @Override
    public void getOrgUnits(Date date) {
        compositeDisposable.add(dataSetDetailRepository.orgUnits()
                .map(data -> {
                    this.orgUnits = data;
                    return OrgUnitUtils.renderTree(view.getContext(), orgUnits, true);
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        treeNode -> view.addTree(treeNode),
                        throwable -> view.renderError(throwable.getMessage())
                ));
    }

    private void updateFilters(String orgUnitQuery) {
        switch (lastSearchType) {
            case LastSearchType.DATES:
                getDataSets(this.fromDate, this.toDate, orgUnitQuery);
                break;
            case LastSearchType.DATE_RANGES:
                getDataSetWithDates(this.dates, this.period, orgUnitQuery);
                break;
            default:
                getDataSetWithDates(null, this.period, orgUnitQuery);
                break;
        }
    }

    @Override
    @SuppressWarnings({"squid:S1172", "squid:CommentedOutCodeLine"})
    public void getDataSetWithDates(List<Date> dates, Period period, String orgUnitQuery) {
        this.dates = dates;
        this.period = period;
        lastSearchType = LastSearchType.DATE_RANGES;
        //TODO cuando haya datos para dataset hay que cambiarlo
        //ahora falla por que se va a hacer la select y no puede
       /* compositeDisposable.add(dataSetDetailRepository.filteredDataSet(programId,"","", categoryOptionComboModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        list ->view.setData(getPeriodFromType(list)),
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
}

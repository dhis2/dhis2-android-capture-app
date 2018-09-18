package org.dhis2.usescases.programEventDetail;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;

import org.dhis2.Bindings.Bindings;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.OrgUnitUtils;
import org.dhis2.utils.Period;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
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

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailInteractor implements ProgramEventDetailContract.Interactor {

    private final MetadataRepository metadataRepository;
    private final ProgramEventDetailRepository programEventDetailRepository;
    private ProgramEventDetailContract.View view;
    private String programId;
    private CompositeDisposable compositeDisposable;
    private CategoryOptionComboModel categoryOptionComboModel;

    private Date fromDate;
    private Date toDate;

    private List<Date> dates;
    private Period period;

    private @LastSearchType
    int lastSearchType;
    private CategoryComboModel mCatCombo;
    private List<OrganisationUnitModel> orgUnits;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LastSearchType.DATES, LastSearchType.DATE_RANGES})
    public @interface LastSearchType {
        int DATES = 1;
        int DATE_RANGES = 32;
    }

    ProgramEventDetailInteractor(ProgramEventDetailRepository programEventDetailRepository, MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
        this.programEventDetailRepository = programEventDetailRepository;
        Bindings.setMetadataRepository(metadataRepository);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(ProgramEventDetailContract.View view, String programId, Period period) {
        this.view = view;
        this.programId = programId;
        getProgram();
        getOrgUnits(null);

        compositeDisposable.add(
                programEventDetailRepository.writePermission(programId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::setWritePermission,
                                Timber::e
                        )
        );
    }

    @Override
    public void getOrgUnits(Date date) {
        compositeDisposable.add(programEventDetailRepository.orgUnits()
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

    @Override
    public Observable<List<String>> getEventDataValueNew(EventModel event) {
        return programEventDetailRepository.eventDataValuesNew(event);
    }


    private void getProgram() {
        compositeDisposable.add(metadataRepository.getProgramWithId(programId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        programModel -> {
                            view.setProgram(programModel);
                            getCatCombo(programModel);
                        },
                        Timber::d)
        );
    }

    private void getCatCombo(ProgramModel programModel) {
        compositeDisposable.add(metadataRepository.getCategoryComboWithId(programModel.categoryCombo())
                .filter(categoryComboModel -> categoryComboModel != null && !categoryComboModel.uid().equals(CategoryComboModel.DEFAULT_UID))
                .flatMap(catCombo -> {
                    mCatCombo = catCombo;
                    return programEventDetailRepository.catCombo(programModel.categoryCombo());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(catComboOptions -> view.setCatComboOptions(mCatCombo, catComboOptions), Timber::d)
        );
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void updateFilters(CategoryOptionComboModel categoryOptionComboModel, String orgUnitQuery) {
        this.categoryOptionComboModel = categoryOptionComboModel;
        switch (lastSearchType) {
            case LastSearchType.DATES:
                getEvents(programId, this.fromDate, this.toDate, orgUnitQuery);
                break;
            case LastSearchType.DATE_RANGES:
                getProgramEventsWithDates(programId, this.dates, this.period, orgUnitQuery);
                break;
            default:
                getProgramEventsWithDates(programId, null, this.period, orgUnitQuery);
                break;
        }
    }

    @Override
    public void getProgramEventsWithDates(String programId, List<Date> dates, Period period, String orgUnitQuery) {
        this.dates = dates;
        this.period = period;
        lastSearchType = LastSearchType.DATE_RANGES;
        compositeDisposable.add(programEventDetailRepository.filteredProgramEvents(programId, dates, period, categoryOptionComboModel, orgUnitQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setData,
                        throwable -> view.renderError(throwable.getMessage())));
    }

    @Override
    public List<OrganisationUnitModel> getOrgUnits() {
        return this.orgUnits;
    }

    @SuppressLint("CheckResult")
    @Override
    public void getEvents(String programId, Date fromDate, Date toDate, String orgUnitQuery) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        lastSearchType = LastSearchType.DATES;
        Observable.just(programEventDetailRepository.filteredProgramEvents(programId,
                DateUtils.getInstance().formatDate(fromDate),
                DateUtils.getInstance().formatDate(toDate),
                categoryOptionComboModel, orgUnitQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setData,
                        Timber::e));
    }
}

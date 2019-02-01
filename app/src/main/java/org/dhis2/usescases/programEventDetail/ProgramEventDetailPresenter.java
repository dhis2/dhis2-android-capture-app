package org.dhis2.usescases.programEventDetail;

import android.os.Bundle;
import androidx.annotation.NonNull;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.OrgUnitUtils;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PROGRAM_UID;


/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailPresenter implements ProgramEventDetailContract.Presenter {

    private final ProgramEventDetailRepository eventRepository;
    private final MetadataRepository metaRepository;
    private ProgramEventDetailContract.View view;
    public ProgramModel program;
    public String programId;
    private CompositeDisposable compositeDisposable;
    private CategoryOptionComboModel categoryOptionComboModel;
    private List<OrganisationUnitModel> orgUnits;

    //Search fields
    private CategoryComboModel mCatCombo;
    private List<Date> dates;
    private Period period;
    private String orgUnitQuery;

    ProgramEventDetailPresenter(
            @NonNull ProgramEventDetailRepository programEventDetailRepository,
            @NonNull MetadataRepository metadataRepository) {
        this.eventRepository = programEventDetailRepository;
        this.metaRepository = metadataRepository;
    }

    @Override
    public void init(ProgramEventDetailContract.View mview, String programId, Period period) {
        view = mview;
        compositeDisposable = new CompositeDisposable();
        this.programId = programId;

        compositeDisposable.add(metaRepository.getProgramWithId(programId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        programModel -> {
                            view.setProgram(programModel);
                            view.setWritePermission(programModel.accessDataWrite());
                            getCatCombo(programModel);
                        },
                        Timber::d)
        );

        compositeDisposable.add(eventRepository.orgUnits()
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

        compositeDisposable.add(
                view.currentPage()
                        .startWith(0)
                        .flatMap(page -> eventRepository.filteredProgramEvents(programId, dates, period, categoryOptionComboModel, orgUnitQuery, page).distinctUntilChanged())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::setData,
                                throwable -> view.renderError(throwable.getMessage())));

    }

    private void getCatCombo(ProgramModel programModel) {
        compositeDisposable.add(metaRepository.getCategoryComboWithId(programModel.categoryCombo())
                .filter(categoryComboModel -> categoryComboModel != null && !categoryComboModel.isDefault() && !categoryComboModel.uid().equals(CategoryComboModel.DEFAULT_UID))
                .flatMap(catCombo -> {
                    this.mCatCombo = catCombo;
                    return eventRepository.catCombo(programModel.categoryCombo());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(catComboOptions -> view.setCatComboOptions(mCatCombo, catComboOptions), Timber::d)
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
    public void setProgram(ProgramModel program) {

        this.program = program;
    }

    @Override
    public List<OrganisationUnitModel> getOrgUnits() {
        return this.orgUnits;
    }

    @Override
    public void setFilters(List<Date> selectedDates, Period currentPeriod, String orgUnits) {
        this.dates = selectedDates;
        this.period = currentPeriod;
        this.orgUnitQuery = orgUnits;
    }

    @Override
    public void onCatComboSelected(CategoryOptionComboModel categoryOptionComboModel) {
        this.categoryOptionComboModel = categoryOptionComboModel;

    }

    @Override
    public void clearCatComboFilters() {
        this.categoryOptionComboModel = null;

    }

    @Override
    public void onEventClick(String eventId, String orgUnit) {
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, programId);
        bundle.putString(Constants.EVENT_UID, eventId);
        bundle.putString(ORG_UNIT, orgUnit);
//        view.startActivity(EventInitialActivity.class, bundle, false, false, null);

        view.startActivity(EventCaptureActivity.class,
                EventCaptureActivity.getActivityBundle(eventId, programId),
                false, false, null
        );
    }

    @Override
    public Observable<List<String>> getEventDataValueNew(EventModel event) {
        return eventRepository.eventDataValuesNew(event);
    }

    public void addEvent() {
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, programId);
        view.startActivity(EventInitialActivity.class, bundle, false, false, null);
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    @Override
    public void showFilter() {
        view.showHideFilter();
    }
}

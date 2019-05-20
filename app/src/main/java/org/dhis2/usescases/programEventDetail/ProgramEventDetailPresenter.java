package org.dhis2.usescases.programEventDetail;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.main.program.SyncStatusDialog;
import org.dhis2.utils.Constants;
import org.dhis2.utils.OrgUnitUtils;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PROGRAM_UID;


/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailPresenter implements ProgramEventDetailContract.Presenter {

    private final ProgramEventDetailRepository eventRepository;
    private ProgramEventDetailContract.View view;
    protected ProgramModel program;
    protected String programId;
    private CompositeDisposable compositeDisposable;
    private List<OrganisationUnitModel> orgUnits = new ArrayList<>();
    private FlowableProcessor<Pair<TreeNode, String>> parentOrgUnit;
    private FlowableProcessor<Trio<List<DatePeriod>, List<String>, List<CategoryOptionCombo>>> programQueries;

    //Search fields
    private List<DatePeriod> currentDateFilter;
    private List<String> currentOrgUnitFilter;
    private List<CategoryOptionCombo> currentCatOptionCombo;

    ProgramEventDetailPresenter(
            @NonNull String programUid, @NonNull ProgramEventDetailRepository programEventDetailRepository) {
        this.eventRepository = programEventDetailRepository;
        this.programId = programUid;
        this.currentCatOptionCombo = new ArrayList<>();
    }

    @Override
    public void init(ProgramEventDetailContract.View view, Period period) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();
        this.currentOrgUnitFilter = new ArrayList<>();
        this.currentDateFilter = new ArrayList<>();
        programQueries = PublishProcessor.create();
        parentOrgUnit = PublishProcessor.create();

        compositeDisposable.add(Observable.just(eventRepository.getAccessDataWrite())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setWritePermission,
                        Timber::e)
        );

        compositeDisposable.add(
                eventRepository.program()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.computation())
                        .subscribe(
                                view::setProgram,
                                Timber::e
                        )
        );

        compositeDisposable.add(
                eventRepository.catCombo()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.computation())
                        .subscribe(
                                view::setCatComboOptions,
                                Timber::e
                        )
        );

        compositeDisposable.add(
                programQueries
                        .startWith(Trio.create(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()))
                        .map(dates_ou_coc -> eventRepository.filteredProgramEvents(dates_ou_coc.val0(), dates_ou_coc.val1(), dates_ou_coc.val2()))
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::setLiveData,
                                throwable -> view.renderError(throwable.getMessage())
                        ));

        compositeDisposable.add(
                parentOrgUnit
                        .flatMap(orgUnit -> eventRepository.orgUnits(orgUnit.val1()).toFlowable(BackpressureStrategy.LATEST)
                                .map(orgUnits1 -> OrgUnitUtils.createNode(view.getContext(), orgUnits, true))
                                .map(nodeList -> Pair.create(orgUnit.val0(), nodeList)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.addNodeToTree(),
                                Timber::e
                        ));
    }

    @Override
    public void updateDateFilter(List<DatePeriod> datePeriodList) {
        this.currentDateFilter = datePeriodList;
        programQueries.onNext(Trio.create(currentDateFilter, currentOrgUnitFilter, currentCatOptionCombo));
    }

    @Override
    public void updateOrgUnitFilter(List<String> orgUnitList) {
        this.currentOrgUnitFilter = orgUnitList;
        programQueries.onNext(Trio.create(currentDateFilter, currentOrgUnitFilter, currentCatOptionCombo));
    }

    @Override
    public void updateCatOptCombFilter(List<CategoryOption> categoryOptionComboMap) {
        this.currentCatOptionCombo = eventRepository.catOptionCombo(categoryOptionComboMap);
        programQueries.onNext(Trio.create(currentDateFilter, currentOrgUnitFilter, currentCatOptionCombo));
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
        if (orgUnits.isEmpty()) {
            view.orgUnitProgress(true);
            compositeDisposable.add(
                    eventRepository.orgUnits()
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    data -> {
                                        this.orgUnits = data;
                                        view.orgUnitProgress(false);
                                        view.addTree(OrgUnitUtils.renderTree(view.getContext(), orgUnits, true));
                                    },
                                    throwable -> view.renderError(throwable.getMessage())));
        }
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
    public void onExpandOrgUnitNode(TreeNode treeNode, String parentUid) {
        parentOrgUnit.onNext(Pair.create(treeNode, parentUid));

    }

    @Override
    public void onSyncIconClick(String uid) {
        view.showSyncDialog(uid, SyncStatusDialog.ConflictType.EVENT);
    }

    @Override
    public void onCatComboSelected(CategoryOptionComboModel categoryOptionComboModel) {

    }

    @Override
    public void clearCatComboFilters() {

    }

    @Override
    public void onEventClick(String eventId, String orgUnit) {
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, programId);
        bundle.putString(Constants.EVENT_UID, eventId);
        bundle.putString(ORG_UNIT, orgUnit);
        view.startActivity(EventCaptureActivity.class,
                EventCaptureActivity.getActivityBundle(eventId, programId),
                false, false, null
        );
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

package org.dhis2.usescases.main.program;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity;
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity;
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.OrgUnitUtils;
import org.dhis2.utils.Period;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.ProgramType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * Created by ppajuelo on 18/10/2017.f
 */

public class ProgramPresenter implements ProgramContract.Presenter {

    private ProgramContract.View view;
    private final HomeRepository homeRepository;
    private CompositeDisposable compositeDisposable;

    private List<OrganisationUnit> myOrgs = new ArrayList<>();
    private FlowableProcessor<Pair<List<DatePeriod>, List<String>>> programQueries;

    private FlowableProcessor<Pair<TreeNode, String>> parentOrgUnit;
    private List<DatePeriod> currentDateFilter;
    private List<String> currentOrgUnitFilter;
    private FlowableProcessor<Boolean> processorDismissDialog;

    ProgramPresenter(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
    }

    @Override
    public void init(ProgramContract.View view) {
        this.view = view;
        this.currentOrgUnitFilter = new ArrayList<>();
        this.currentDateFilter = new ArrayList<>();
        this.compositeDisposable = new CompositeDisposable();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        programQueries = PublishProcessor.create();
        parentOrgUnit = PublishProcessor.create();
        this.processorDismissDialog = PublishProcessor.create();

        if(FilterManager.getInstance().getPeriodFilters().size() != 0)
            currentDateFilter = FilterManager.getInstance().getPeriodFilters();
        if(FilterManager.getInstance().getOrgUnitFilters().size() != 0)
            currentOrgUnitFilter = FilterManager.getInstance().getOrgUnitUidsFilters();

        compositeDisposable.add(
                programQueries
                        .startWith(Pair.create(currentDateFilter, currentOrgUnitFilter))
                        .flatMap(datePeriodOrgs -> Flowable.zip(homeRepository.programModels(datePeriodOrgs.val0(), datePeriodOrgs.val1(), FilterManager.getInstance().getStateFilters()),
                                homeRepository.aggregatesModels(datePeriodOrgs.val0(), datePeriodOrgs.val1(), FilterManager.getInstance().getStateFilters()), (programs, dataSets) -> {
                                    programs.addAll(dataSets);
                                    Collections.sort(programs, (program1, program2) -> program1.title().compareToIgnoreCase(program2.title()));
                                    return programs;
                                }))
                        .subscribeOn(Schedulers.from(executorService))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.swapProgramModelData(),
                                throwable -> view.renderError(throwable.getMessage())
                        ));

        compositeDisposable.add(
                parentOrgUnit
                        .flatMap(orgUnit -> homeRepository.orgUnits(orgUnit.val1()).toFlowable(BackpressureStrategy.LATEST)
                                .map(this::transformToNode)
                                .map(nodeList -> Pair.create(orgUnit.val0(), nodeList)))
                        .subscribeOn(Schedulers.from(executorService))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.addNodeToTree(),
                                Timber::e
                        ));

        compositeDisposable.add(
                FilterManager.getInstance().asFlowable()
                        .subscribeOn(Schedulers.io())
                        .flatMap(filterManager ->
                                homeRepository.programModels(filterManager.getPeriodFilters(), filterManager.getOrgUnitUidsFilters(),filterManager.getStateFilters()).flatMapIterable(data -> data)
                                        .mergeWith(homeRepository.aggregatesModels(filterManager.getPeriodFilters(), filterManager.getOrgUnitUidsFilters(),filterManager.getStateFilters()).flatMapIterable(data -> data))
                                        .sorted((p1, p2) -> p1.title().compareToIgnoreCase(p2.title())).toList().toFlowable()
                                        .subscribeOn(Schedulers.io()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.swapProgramModelData(),
                                throwable -> view.renderError(throwable.getMessage())
                        )
        );

        compositeDisposable.add(
                FilterManager.getInstance().ouTreeFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                open -> view.openOrgUnitTreeSelector(),
                                Timber::e
                        )
        );

        manageProcessorDismissDialog();
    }

    private void manageProcessorDismissDialog(){
        compositeDisposable.add(processorDismissDialog
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bool -> init(view), Timber::d));
    }

    @Override
    public void onExpandOrgUnitNode(TreeNode treeNode, String parentUid) {
        parentOrgUnit.onNext(Pair.create(treeNode, parentUid));
    }

    @Override
    public List<TreeNode> transformToNode(List<OrganisationUnit> orgUnits) {
        return OrgUnitUtils.createNode_2(view.getContext(), orgUnits, true);
    }

    @Override
    public List<OrganisationUnit> getOrgUnits() {
        return myOrgs;
    }

    @Override
    public void dispose() {
        if (!myOrgs.isEmpty())
            myOrgs.clear();
        compositeDisposable.clear();
    }

    @Override
    public void updateDateFilter(List<DatePeriod> datePeriodList) {
        this.currentDateFilter = datePeriodList;
        programQueries.onNext(Pair.create(currentDateFilter, currentOrgUnitFilter));
    }

    @Override
    public void updateOrgUnitFilter(List<String> orgUnitList) {
        this.currentOrgUnitFilter = orgUnitList;
        programQueries.onNext(Pair.create(currentDateFilter, currentOrgUnitFilter));
    }

    @Override
    public void onSyncStatusClick(ProgramViewModel program) {
        if(!program.typeName().equals("DataSets"))
            view.showSyncDialog(program.id(), SyncStatusDialog.ConflictType.PROGRAM, processorDismissDialog);
        else
            view.showSyncDialog(program.id(), SyncStatusDialog.ConflictType.DATA_SET, processorDismissDialog);
    }

    @Override
    public boolean areFiltersApplied() {
        return !currentDateFilter.isEmpty() || !currentOrgUnitFilter.isEmpty();
    }

    @Override
    public void onItemClick(ProgramViewModel programModel, Period currentPeriod) {

        Bundle bundle = new Bundle();
        String idTag;
        if (!isEmpty(programModel.type())) {
            bundle.putString("TRACKED_ENTITY_UID", programModel.type());
        }

        if (programModel.typeName().equals("DataSets"))
            idTag = "DATASET_UID";
        else
            idTag = "PROGRAM_UID";

        bundle.putString(idTag, programModel.id());
        bundle.putString(Constants.DATA_SET_NAME, programModel.title());
        bundle.putString(Constants.ACCESS_DATA, programModel.accessDataWrite().toString());
        int programTheme = ColorUtils.getThemeFromColor(programModel.color());
        SharedPreferences prefs = view.getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        if (programTheme != -1) {
            prefs.edit().putInt(Constants.PROGRAM_THEME, programTheme).apply();
        } else
            prefs.edit().remove(Constants.PROGRAM_THEME).apply();

        if (programModel.programType().equals(ProgramType.WITH_REGISTRATION.name())) {
            view.startActivity(SearchTEActivity.class, bundle, false, false, null);
        } else if (programModel.programType().equals(ProgramType.WITHOUT_REGISTRATION.name())) {
            ArrayList<Date> dates = new ArrayList<>();
            switch (currentPeriod) {
                case DAILY:
                    dates.add(view.getChosenDateDay());
                    break;
                case WEEKLY:
                    dates.addAll(view.getChosenDateWeek());
                    break;
                case MONTHLY:
                    dates.addAll(view.getChosenDateMonth());
                    break;
                case YEARLY:
                    dates.addAll(view.getChosenDateYear());
                    break;
            }

            view.startActivity(ProgramEventDetailActivity.class,
                    ProgramEventDetailActivity.getBundle(programModel.id(), currentPeriod.name(), dates),
                    false, false, null);
        } else {
            view.startActivity(DataSetDetailActivity.class, bundle, false, false, null);
        }
    }

    @Override
    public void onOrgUnitButtonClick() {
        view.openDrawer();
        if (myOrgs.isEmpty()) {
            view.orgUnitProgress(true);
            compositeDisposable.add(
                    homeRepository.orgUnits()
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    data -> {
                                        this.myOrgs = data;
                                        view.orgUnitProgress(false);
                                        view.addTree(OrgUnitUtils.renderTree_2(view.getContext(), myOrgs, true, ""));
                                    },
                                    throwable -> view.renderError(throwable.getMessage())));
        }
    }

    @Override
    public void onDateRangeButtonClick() {
        view.showRageDatePicker();
    }


    @Override
    public void onTimeButtonClick() {
        view.showTimeUnitPicker();
    }

    @Override
    public void showDescription(String description) {
        if (!isEmpty(description))
            view.showDescription(description);
    }

    @Override
    public void showHideFilterClick(){
        view.showHideFilter();
    }

    @Override
    public void clearFilterClick() {
        FilterManager.getInstance().clearAllFilters();
        view.clearFilters();
    }
}

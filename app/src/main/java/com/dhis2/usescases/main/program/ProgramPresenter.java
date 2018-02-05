package com.dhis2.usescases.main.program;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.dhis2.R;
import com.dhis2.usescases.programDetail.ProgramDetailActivity;
import com.dhis2.usescases.programDetailTablet.ProgramDetailTabletActivity;
import com.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ppajuelo on 18/10/2017.f
 */

public class ProgramPresenter implements ProgramContractModule.Presenter {

    private ProgramContractModule.View view;
    private final HomeRepository homeRepository;
    private final CompositeDisposable compositeDisposable;

    private Date fromDate, toDate;
    private List<Date> dates;
    private Period period;

    @Inject
    ProgramPresenter(ProgramContractModule.View view, HomeRepository homeRepository) {
        this.view = view;
        this.homeRepository = homeRepository;
        this.compositeDisposable = new CompositeDisposable();
    }

    void init() {
        getPrograms(DateUtils.getInstance().getToday(), DateUtils.getInstance().getToday());
        getOrgUnits();
        compositeDisposable.add(homeRepository.eventModels()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> Log.d("EVENT DATA", "events:" + data.size()),
                        throwable -> view.renderError(throwable.getMessage())));
    }


    public void getPrograms(Date fromDate, Date toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.dates = null;
        this.period = null;
        compositeDisposable.add(homeRepository.programs(
                DateUtils.getInstance().formatDate(fromDate),
                DateUtils.getInstance().formatDate(toDate))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapProgramData(),
                        throwable -> view.renderError(throwable.getMessage())));
    }

    public void getProgramsWithDates(List<Date> dates, Period period) {
        this.fromDate = null;
        this.toDate = null;
        this.dates = dates;
        this.period = period;
        compositeDisposable.add(homeRepository.programs(dates, period)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapProgramData(),
                        throwable -> view.renderError(throwable.getMessage())));
    }

    public void getProgramsOrgUnit(String orgUnitQuery) {
        compositeDisposable.add(homeRepository.programs(dates, period, orgUnitQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapProgramData(),
                        throwable -> view.renderError(throwable.getMessage())
                ));
    }

    @Override
    public void onItemClick(ProgramModel programModel) {
        if (programModel.programType() == ProgramType.WITH_REGISTRATION) {
            Bundle bundle = new Bundle();
            bundle.putString("PROGRAM_UID", programModel.uid());
            if (programModel.displayFrontPageList()) {
                if (view.getContext().getResources().getBoolean(R.bool.is_tablet))
                    view.startActivity(ProgramDetailTabletActivity.class, bundle, false, false, null);
                else
                    view.startActivity(ProgramDetailActivity.class, bundle, false, false, null);
            } else
                view.startActivity(SearchTEActivity.class, bundle, false, false, null);
        }
    }

    @Override
    public void onOrgUnitButtonClick() {
        view.openDrawer();
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
    public void getOrgUnits() {

        compositeDisposable.add(homeRepository.orgUnits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::renderTree,
                        throwable -> view.renderError(throwable.getMessage())
                ));
    }


    private void renderTree(@NonNull List<OrganisationUnitModel> myOrgs) {

        HashMap<Integer, ArrayList<TreeNode>> subLists = new HashMap<>();

        List<OrganisationUnitModel> allOrgs = new ArrayList<>();
        allOrgs.addAll(myOrgs);
        for (OrganisationUnitModel myorg : myOrgs) {
            String[] path = myorg.path().split("/");
            for (int i = myorg.level() - 1; i > 0; i--) {
                OrganisationUnitModel orgToAdd = OrganisationUnitModel.builder()
                        .uid(path[i])
                        .level(i)
                        .parent(path[i - 1])
                        .name(path[i])
                        .displayName(path[i])
                        .displayShortName(path[i])
                        .build();
                if (!allOrgs.contains(orgToAdd))
                    allOrgs.add(orgToAdd);
            }
        }

        Collections.sort(myOrgs, (org1, org2) -> org2.level().compareTo(org1.level()));

        for (int i = 0; i < myOrgs.get(0).level(); i++) {
            subLists.put(i + 1, new ArrayList<>());
        }

        //Separamos las orunits en listas por nivel
        for (OrganisationUnitModel orgs : allOrgs) {
            ArrayList<TreeNode> sublist = subLists.get(orgs.level());
            TreeNode treeNode = new TreeNode(orgs).setViewHolder(new OrgUnitHolder(view.getContext()));
            treeNode.setSelectable(orgs.path() != null);
            sublist.add(treeNode);
            subLists.put(orgs.level(), sublist);
        }

        TreeNode root = TreeNode.root();
        root.addChildren(subLists.get(1));

        for (int level = myOrgs.get(0).level(); level > 1; level--) {
            for (TreeNode treeNode : subLists.get(level - 1)) {
                for (TreeNode treeNodeLevel : subLists.get(level)) {
                    if (((OrganisationUnitModel) treeNodeLevel.getValue()).parent().equals(((OrganisationUnitModel) treeNode.getValue()).uid()))
                        treeNode.addChild(treeNodeLevel);
                }
            }
        }

        view.addTree(root);

    }

    @Override
    public Observable<List<EventModel>> getEvents(ProgramModel programModel) {
        return homeRepository.eventModels(programModel.uid());
    }
}

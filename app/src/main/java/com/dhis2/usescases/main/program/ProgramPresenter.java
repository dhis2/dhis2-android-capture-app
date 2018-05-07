package com.dhis2.usescases.main.program;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.dhis2.R;
import com.dhis2.usescases.programDetail.ProgramDetailActivity;
import com.dhis2.usescases.programDetailTablet.ProgramDetailTabletActivity;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailActivity;
import com.dhis2.usescases.searchTrackEntity.SearchTEActivity;
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

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ppajuelo on 18/10/2017.f
 */

public class ProgramPresenter implements ProgramContract.Presenter {

    private ProgramContract.View view;
    private final HomeRepository homeRepository;
    private final CompositeDisposable compositeDisposable;

    private List<OrganisationUnitModel> myOrgs;

    ProgramPresenter(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
        this.compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(ProgramContract.View view) {
        this.view = view;
        this.view = view;

        compositeDisposable.add(
                homeRepository.orgUnits()
                        .map(
                                orgUnits -> {
                                    this.myOrgs = orgUnits;
                                    return homeRepository.programs(orgUnitQuery());
                                }
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    view.swapProgramData().accept(data.blockingFirst());
                                    renderTree(myOrgs);
                                },
                                throwable -> view.renderError(throwable.getMessage())));
    }


    @Override
    public void getProgramsWithDates(ArrayList<Date> dates, Period period) {
        compositeDisposable.add(homeRepository.programs(dates, period)
//                .flatMap(data->homeRepository.toDoPrograms(dates, period))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapProgramData(),
                        throwable -> view.renderError(throwable.getMessage())));

    }


    @Override
    public void getProgramsOrgUnit(List<Date> dates, Period period, String orgUnitQuery) {
        compositeDisposable.add(homeRepository.programs(dates, period, orgUnitQuery)
//                .flatMap(data->homeRepository.toDoPrograms(dates, period, orgUnitQuery))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapProgramData(),
                        throwable -> view.renderError(throwable.getMessage())
                ));
    }

    @Override
    public void getAllPrograms(String orgUnitQuery) {
        compositeDisposable.add(
                homeRepository.programs(orgUnitQuery)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.swapProgramData(),
                                throwable -> view.renderError(throwable.getMessage())
                        ));
    }

    @Override
    public void onItemClick(ProgramModel programModel, Period currentPeriod) {

        Bundle bundle = new Bundle();
        bundle.putString("PROGRAM_UID", programModel.uid());
        bundle.putString("TRACKED_ENTITY_UID", programModel.trackedEntityType());

        switch (currentPeriod) {
            case NONE:
                bundle.putInt("CURRENT_PERIOD", R.string.period);
                bundle.putSerializable("CHOOSEN_DATE", null);
            case DAILY:
                bundle.putInt("CURRENT_PERIOD", R.string.DAILY);
                bundle.putSerializable("CHOOSEN_DATE", view.getChosenDateDay());
                break;
            case WEEKLY:
                bundle.putInt("CURRENT_PERIOD", R.string.WEEKLY);
                bundle.putSerializable("CHOOSEN_DATE", view.getChosenDateWeek());
                break;
            case MONTHLY:
                bundle.putInt("CURRENT_PERIOD", R.string.MONTHLY);
                bundle.putSerializable("CHOOSEN_DATE", view.getChosenDateMonth());
                break;
            case YEARLY:
                bundle.putInt("CURRENT_PERIOD", R.string.YEARLY);
                bundle.putSerializable("CHOOSEN_DATE", view.getChosenDateYear());
                break;
        }


        if (programModel.programType() == ProgramType.WITH_REGISTRATION) {
            if (programModel.displayFrontPageList()) {
                if (view.getContext().getResources().getBoolean(R.bool.is_tablet)) {
                    view.startActivity(ProgramDetailTabletActivity.class, bundle, false, false, null);
                } else {
                    view.startActivity(ProgramDetailActivity.class, bundle, false, false, null);
                }
            } else {
                view.startActivity(SearchTEActivity.class, bundle, false, false, null);
            }
        } else {
            view.startActivity(ProgramEventDetailActivity.class, bundle, false, false, null);
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
    public void showDescription(String description) {
        view.showDescription(description);
    }

    private void renderTree(@NonNull List<OrganisationUnitModel> myOrgs) {

        HashMap<Integer, ArrayList<TreeNode>> subLists = new HashMap<>();

        List<OrganisationUnitModel> allOrgs = new ArrayList<>();
        allOrgs.addAll(myOrgs);
        for (OrganisationUnitModel myorg : myOrgs) {
            String[] pathName = myorg.displayNamePath().split("/");
            String[] pathUid = myorg.path().split("/");
            for (int i = myorg.level() - 1; i > 0; i--) {
                OrganisationUnitModel orgToAdd = OrganisationUnitModel.builder()
                        .uid(pathUid[i])
                        .level(i)
                        .parent(pathUid[i - 1])
                        .name(pathName[i])
                        .displayName(pathName[i])
                        .displayShortName(pathName[i])
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

    private String orgUnitQuery() {
        StringBuilder orgUnitFilter = new StringBuilder();
        for (int i = 0; i < myOrgs.size(); i++) {
            orgUnitFilter.append("'");
            orgUnitFilter.append(myOrgs.get(i).uid());
            orgUnitFilter.append("'");
            if (i < myOrgs.size() - 1)
                orgUnitFilter.append(", ");
        }
        view.setOrgUnitFilter(orgUnitFilter);
        return orgUnitFilter.toString();
    }
}

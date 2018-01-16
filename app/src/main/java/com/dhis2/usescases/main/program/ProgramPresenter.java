package com.dhis2.usescases.main.program;

import android.util.Log;

import com.dhis2.utils.DateUtils;
import com.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramType;

import java.util.ArrayList;
import java.util.Date;
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

    @Inject
    ProgramPresenter(ProgramContractModule.View view, HomeRepository homeRepository) {
        this.view = view;
        this.homeRepository = homeRepository;
        this.compositeDisposable = new CompositeDisposable();
    }

    void init() {

        getPrograms(DateUtils.getInstance().getToday(), DateUtils.getInstance().getToday());
        getOrgUnits();
    }


    void searchProgramByOrgUnit(ArrayList<String> ids) {
//        compositeDisposable.add(homeRepository.homeViewModels(ids)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                        view.swapProgramData(),
//                        throwable -> view.renderError(throwable.getMessage())));
    }

    public void getPrograms(Date fromDate, Date toDate) {
        compositeDisposable.add(homeRepository.programs(
                DateUtils.getInstance().formatDate(fromDate),
                DateUtils.getInstance().formatDate(toDate))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapProgramData(),
                        throwable -> view.renderError(throwable.getMessage())));

        compositeDisposable.add(homeRepository.eventModels(null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> Log.d("DATA", "myData"),
                        throwable -> view.renderError(throwable.getMessage())));
    }

    public void getProgramsWithDates(List<Date> dates, Period period) {

        compositeDisposable.add(homeRepository.programs(dates, period)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapProgramData(),
                        throwable -> view.renderError(throwable.getMessage())));
    }

    @Override
    public void onItemClick(ProgramModel homeViewModel) {
        if (homeViewModel.programType() == ProgramType.WITH_REGISTRATION) {
          /*  Bundle bundle = new Bundle();
            bundle.putSerializable("PROGRAM", homeViewModel);
            if (view.getContext().getResources().getBoolean(R.bool.is_tablet))
                view.startActivity(ProgramDetailTabletActivity.class, bundle, false, false, null);
            else
                view.startActivity(ProgramDetailActivity.class, bundle, false, false, null);*/
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

        compositeDisposable.add(homeRepository.trackedEntities()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        trackEntities -> Log.d("TEST", "Numero total de track entities = " + trackEntities.size()),
                        throwable -> view.renderError(throwable.getMessage())
                ));
    }

    private void renderTree(List<OrganisationUnitModel> myOrgs) {

        TreeNode root = TreeNode.root();
        ArrayList<TreeNode> allTreeNodes = new ArrayList<>();
        ArrayList<TreeNode> treeNodesToRemove = new ArrayList<>();

        int maxLevel = -1;
        int minLevel = 999;
        for (OrganisationUnitModel orgUnit : myOrgs) {
            maxLevel = orgUnit.level() > maxLevel ? orgUnit.level() : maxLevel;
            minLevel = orgUnit.level() < minLevel ? orgUnit.level() : minLevel;
            allTreeNodes.add(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext())));
        }

        for (TreeNode treeNodeParent : allTreeNodes) {
            for (TreeNode treeNodeChild : allTreeNodes) {
                OrganisationUnitModel parentOU = ((OrganisationUnitModel) treeNodeParent.getValue());
                OrganisationUnitModel childOU = ((OrganisationUnitModel) treeNodeChild.getValue());

                if (childOU.parent() != null && childOU.parent().equals(parentOU.uid())) {
                    treeNodeParent.addChildren(treeNodeChild);
                    treeNodesToRemove.add(treeNodeChild);
                }
            }
        }

        allTreeNodes.remove(treeNodesToRemove);

        for (TreeNode treeNode : allTreeNodes) {
            root.addChild(treeNode);
        }


        view.addTree(root);


    }

    @Override
    public Observable<List<EventModel>> getEvents(ProgramModel programModel) {
        return homeRepository.eventModels(programModel.uid());
    }
}

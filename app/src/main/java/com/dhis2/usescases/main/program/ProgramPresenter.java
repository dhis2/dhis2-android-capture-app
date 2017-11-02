package com.dhis2.usescases.main.program;

import android.util.Log;

import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ppajuelo on 18/10/2017.
 */

public class ProgramPresenter implements ProgramContractModule.Presenter {

    ProgramContractModule.View view;
    ProgramContractModule.Router router;
    private final HomeRepository homeRepository;
    private final CompositeDisposable compositeDisposable;

    @Inject
    ProgramPresenter(ProgramContractModule.View view, HomeRepository homeRepository) {
        this.view = view;
        this.router = new ProgramRouter(view);
        this.homeRepository = homeRepository;
        this.compositeDisposable = new CompositeDisposable();
    }

    void init() {

        compositeDisposable.add(homeRepository.homeViewModels()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapData(),
                        throwable -> view.renderError(throwable.getMessage())));

        getOrgUnits(null);

    }

    public void searchProgramByOrgUnit(ArrayList<String> ids) {
        compositeDisposable.add(homeRepository.homeViewModels(ids)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapData(),
                        throwable -> view.renderError(throwable.getMessage())));
    }


    @Override
    public void onItemClick(HomeViewModel homeViewModel) {
        router.goToProgramDetail(homeViewModel);
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
    public void onCatComboButtonClick() {

    }

    @Override
    public void getOrgUnits(List<OrganisationUnitModel> orgs) {

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

                if (childOU.parent().equals(parentOU.uid())) {
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
}

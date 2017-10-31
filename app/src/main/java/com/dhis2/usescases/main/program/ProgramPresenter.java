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
        router.goToProgramDetail();
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

        List<OrganisationUnitModel> totalOrgs = new ArrayList<>();
        List<OrganisationUnitModel> toRemove = new ArrayList<>();

        totalOrgs.addAll(myOrgs);

        TreeNode root = TreeNode.root();


        //LEVEL 1
        for (OrganisationUnitModel orgUnit : totalOrgs) {
            if (orgUnit.level() == 1) {
                root.addChild(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext())));
                toRemove.add(orgUnit);
            }
        }

        totalOrgs.removeAll(toRemove);
        toRemove.clear();


        //LEVEL 2
        for (TreeNode level1 : root.getChildren()) {
            for (OrganisationUnitModel orgUnit : totalOrgs) {
                if (orgUnit.level() == 2 && ((OrganisationUnitModel) level1.getValue()).uid().equals(orgUnit.parent())) {
                    level1.addChild(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext())));
                }
            }
        }

        totalOrgs.removeAll(toRemove);
        toRemove.clear();

        //LEVEL 3
        for (TreeNode level1 : root.getChildren()) {
            for (TreeNode level2 : level1.getChildren()) {
                for (OrganisationUnitModel orgUnit : totalOrgs) {
                    if (orgUnit.level() == 3 && ((OrganisationUnitModel) level2.getValue()).uid().equals(orgUnit.parent())) {
                        level2.addChild(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext())));
                    }
                }
            }
        }

        totalOrgs.removeAll(toRemove);
        toRemove.clear();

        //LEVEL 4
        for (TreeNode level1 : root.getChildren()) {
            for (TreeNode level2 : level1.getChildren()) {
                for (TreeNode level3 : level2.getChildren()) {
                    for (OrganisationUnitModel orgUnit : totalOrgs) {
                        if (orgUnit.level() == 4 && ((OrganisationUnitModel) level3.getValue()).uid().equals(orgUnit.parent())) {
                            level3.addChild(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext())));
                        }
                    }
                }
            }
        }


        view.addTree(root);


    }
}

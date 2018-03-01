package com.dhis2.usescases.eventInitial;

import android.support.annotation.NonNull;

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.main.program.OrgUnitHolder;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventInitialInteractor implements EventInitialContract.Interactor {

    private final MetadataRepository metadataRepository;
    private final EventInitialRepository eventInitialRepository;
    private EventInitialContract.View view;
    private String programId;
    private String eventId;
    private CompositeDisposable compositeDisposable;
    private CategoryOptionComboModel categoryOptionComboModel;


    EventInitialInteractor(EventInitialRepository eventInitialRepository, MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
        this.eventInitialRepository = eventInitialRepository;
        Bindings.setMetadataRepository(metadataRepository);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(EventInitialContract.View view, String programId, String eventId) {
        this.view = view;
        this.programId = programId;
        this.eventId = eventId;
        getProgram();
        getEvent();
        getOrgUnits();
    }

    @Override
    public void onDettach() {
        compositeDisposable.dispose();
    }

    private void getEvent(){
        compositeDisposable.add(eventInitialRepository.event(eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (eventModel) -> {
                            view.setEvent(eventModel);
                        },
                        Timber::d)
        );
    }

    private void getProgram() {
        compositeDisposable.add(metadataRepository.getProgramWithId(programId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (programModel) -> {
                            view.setProgram(programModel);
                            getCatCombo(programModel);
                        },
                        Timber::d)
        );
    }

    private void getCatCombo(ProgramModel programModel){
        compositeDisposable.add(metadataRepository.getCategoryComboWithId(programModel.categoryCombo())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (catCombo) -> compositeDisposable.add(eventInitialRepository.catCombo(programModel.categoryCombo())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        (catComboOptions) -> view.setCatComboOptions(catCombo, catComboOptions),
                                        Timber::d)
                        ),
                        Timber::d)
        );
    }

    @Override
    public void getOrgUnits() {
        compositeDisposable.add(eventInitialRepository.orgUnits()
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
}

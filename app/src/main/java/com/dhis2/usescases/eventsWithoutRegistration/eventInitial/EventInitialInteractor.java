package com.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.Bindings.Bindings;
import com.dhis2.R;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.main.program.OrgUnitHolder;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.category.CategoryComboModel;
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
    private CompositeDisposable compositeDisposable;
    private ProgramModel programModel;
    private CategoryComboModel catCombo;


    EventInitialInteractor(EventInitialRepository eventInitialRepository, MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
        this.eventInitialRepository = eventInitialRepository;
        Bindings.setMetadataRepository(metadataRepository);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(EventInitialContract.View view, String programId, @Nullable String eventId) {
        this.view = view;

        if (eventId != null)
            compositeDisposable.add(
                    eventInitialRepository.event(eventId)
                            .flatMap(
                                    (eventModel) -> {
                                        view.setEvent(eventModel);
                                        return metadataRepository.getProgramWithId(programId);
                                    }
                            )
                            .flatMap(
                                    programModel -> {
                                        this.programModel = programModel;
                                        view.setProgram(programModel);
                                        return metadataRepository.getCategoryComboWithId(programModel.categoryCombo());
                                    }
                            )
                            .flatMap(
                                    catCombo -> {
                                        this.catCombo = catCombo;
                                        return eventInitialRepository.catCombo(programModel.categoryCombo());
                                    }
                            )
                            .subscribe(
                                    catComboOptions -> view.setCatComboOptions(catCombo, catComboOptions),
                                    Timber::d
                            )
            );
        else
            compositeDisposable.add(
                    metadataRepository.getProgramWithId(programId)
                            .flatMap(
                                    programModel -> {
                                        this.programModel = programModel;
                                        view.setProgram(programModel);
                                        return metadataRepository.getCategoryComboWithId(programModel.categoryCombo());
                                    }
                            )
                            .flatMap(
                                    catCombo -> {
                                        this.catCombo = catCombo;
                                        return eventInitialRepository.catCombo(programModel.categoryCombo());
                                    }
                            )
                            .subscribe(
                                    catComboOptions -> view.setCatComboOptions(catCombo, catComboOptions),
                                    Timber::d
                            )
            );
        getOrgUnits();
        getProgramStage(programId);
    }

    private void getProgramStage(String programUid){
        compositeDisposable.add(eventInitialRepository.programStage(programUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        programStageModel -> view.setProgramStage(programStageModel),
                        throwable -> view.renderError(throwable.getMessage())
                ));
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void getCatOption(String categoryOptionComboId) {
        compositeDisposable.add(metadataRepository.getCategoryOptionComboWithId(categoryOptionComboId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (catOption) -> view.setCatOption(catOption),
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

    @Override
    public void getFilteredOrgUnits(String date) {
        compositeDisposable.add(eventInitialRepository.filteredOrgUnits(date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::renderTree,
                        throwable -> view.renderError(throwable.getMessage())

                ));
    }

    @Override
    public void createNewEvent(String programStageModelUid, String programUid, String date, String orgUnitUid, String catComboUid, String catOptionUid, String latitude, String longitude) {
        long rowId = eventInitialRepository.createEvent(view.getContext(), programUid, programStageModelUid, date, orgUnitUid, catComboUid, catOptionUid, latitude, longitude);
        if (rowId < 0) {
            String message = view.getContext().getString(R.string.failed_insert_event);
            view.showToast(message);
        }
        else {
            compositeDisposable.add(eventInitialRepository.newlyCreatedEvent(rowId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            eventModel -> view.onEventCreated(eventModel.uid()),
                            throwable -> view.renderError(throwable.getMessage())
                    ));
        }
    }


    @Override
    public void editEvent(String eventUid, String date, String orgUnitUid, String catComboUid, String latitude, String longitude) {
        compositeDisposable.add(eventInitialRepository.editEvent(eventUid, date, orgUnitUid, catComboUid, latitude, longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (eventModel) -> view.onEventUpdated(eventModel.uid()),
                        throwable -> view.renderError(throwable.getMessage())

                ));
    }

    @Override
    public void onDetach() {
        compositeDisposable.dispose();
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

        if (!myOrgs.isEmpty() && myOrgs.get(0).level() != null) {
            for (int i = 0; i < myOrgs.get(0).level(); i++) {
                subLists.put(i + 1, new ArrayList<>());
            }
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
        if (subLists.size() > 0) {
            root.addChildren(subLists.get(1));
        }

        if (!myOrgs.isEmpty() && myOrgs.get(0).level() != null) {
            for (int level = myOrgs.get(0).level(); level > 1; level--) {
                for (TreeNode treeNode : subLists.get(level - 1)) {
                    for (TreeNode treeNodeLevel : subLists.get(level)) {
                        if (((OrganisationUnitModel) treeNodeLevel.getValue()).parent().equals(((OrganisationUnitModel) treeNode.getValue()).uid()))
                            treeNode.addChild(treeNodeLevel);
                    }
                }
            }
        }

        view.addTree(root);
    }
}

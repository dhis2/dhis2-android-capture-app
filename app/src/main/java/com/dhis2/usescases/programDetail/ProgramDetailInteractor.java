package com.dhis2.usescases.programDetail;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.user.UserRepository;
import com.dhis2.usescases.main.program.OrgUnitHolder;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ppajuelo on 31/10/2017 .
 *
 */

public class ProgramDetailInteractor implements ProgramDetailContractModule.Interactor {

    private final MetadataRepository metadataRepository;
    private ProgramDetailContractModule.View view;
    private String programId;
    private UserRepository userRepository;
    private CompositeDisposable compositeDisposable;
    private ArrayList<OrganisationUnitModel> selectedOrgUnits = new ArrayList<>();

    ProgramDetailInteractor(@NonNull UserRepository userRepository, MetadataRepository metadataRepository) {
        this.userRepository = userRepository;
        this.metadataRepository = metadataRepository;
        Bindings.setMetadataRepository(metadataRepository);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(ProgramDetailContractModule.View view, String programId) {
        this.view = view;
        this.programId = programId;
        getProgram();
        getOrgUnits();
        getData();
    }

    private void getProgram() {
        compositeDisposable.add(metadataRepository.getProgramWithId(programId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        programModel -> view.setProgram(programModel),
                        Timber::d)
        );
    }

    private void getOrgUnits() {
        compositeDisposable.add(userRepository.myOrgUnits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        orgsUnits -> {
                            view.setOrgUnitNames(orgsUnits);
                            renderTree(orgsUnits);
                        },
                        Timber::d)
        );
    }

    @Override
    @SuppressLint("CheckResult")
    public void getData(){
        Observable.zip(
                metadataRepository.getTrackedEntityInstances(programId),
                metadataRepository.getProgramTrackedEntityAttributes(programId),
                (trackedEntityInstanceModelList, programTrackedEntityAttributeModelList) -> {

                    List<MyTrackedEntityInstance> myTrackedEntityInstanceList = new ArrayList<>();
                    for (TrackedEntityInstanceModel trackedEntityInstanceModel : trackedEntityInstanceModelList) {
                        MyTrackedEntityInstance myTrackedEntityInstance = new MyTrackedEntityInstance(trackedEntityInstanceModel);
                        myTrackedEntityInstanceList.add(myTrackedEntityInstance);
                    }

                    TrackedEntityObject trackedEntityObject = new TrackedEntityObject(myTrackedEntityInstanceList, programTrackedEntityAttributeModelList);

                    for (MyTrackedEntityInstance myTrackedEntityInstance : trackedEntityObject.getMyTrackedEntityInstances()) {
                        compositeDisposable.add(metadataRepository.getTEIAttributeValues(programId, myTrackedEntityInstance.getTrackedEntityInstance().uid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        myTrackedEntityInstance::setTrackedEntityAttributeValues,
                                        Timber::d)
                        );

                        compositeDisposable.add(metadataRepository.getTEIEnrollments(myTrackedEntityInstance.getTrackedEntityInstance().uid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        (enrollments) -> {
                                            myTrackedEntityInstance.setEnrollments(enrollments);
                                            for (EnrollmentModel enrollmentModel : enrollments){
                                                compositeDisposable.add(metadataRepository.getEnrollmentEvents(enrollmentModel.uid())
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(
                                                                myTrackedEntityInstance::setEventModels,
                                                                Timber::d)
                                                );
                                            }
                                        },
                                        Timber::d)
                        );
                    }
                    return trackedEntityObject;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (trackedEntityObject) -> {
                            view.swapData(trackedEntityObject);
                            view.setAttributeOrder(trackedEntityObject.getProgramTrackedEntityAttributes());
                        },
                        Timber::d);
    }

    private void renderTree(List<OrganisationUnitModel> myOrgs) {
        selectedOrgUnits.addAll(myOrgs);
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

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }
}
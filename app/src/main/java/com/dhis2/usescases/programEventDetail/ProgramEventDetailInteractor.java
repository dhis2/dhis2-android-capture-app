package com.dhis2.usescases.programEventDetail;

import android.support.annotation.NonNull;

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.user.UserRepository;
import com.dhis2.usescases.main.program.OrgUnitHolder;
import com.dhis2.usescases.programDetail.TrackedEntityObject;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import timber.log.Timber;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

public class ProgramEventDetailInteractor implements ProgramEventDetailContract.Interactor {

    private final MetadataRepository metadataRepository;
    private final ProgramEventDetailRepository programEventDetailRepository;
    ProgramEventDetailContract.View view;
    private D2 d2;
    private String ouMode = "DESCENDANTS";
    private String programId;
    private UserRepository userRepository;
    private CompositeDisposable compositeDisposable;
    private ArrayList<OrganisationUnitModel> selectedOrgUnits = new ArrayList<>();
    private Call<TrackedEntityObject> currentCall;

    ProgramEventDetailInteractor(D2 d2, @NonNull UserRepository userRepository, ProgramEventDetailRepository programEventDetailRepository, MetadataRepository metadataRepository) {
        this.d2 = d2;
        this.userRepository = userRepository;
        this.metadataRepository = metadataRepository;
        this.programEventDetailRepository = programEventDetailRepository;
        Bindings.setMetadataRepository(metadataRepository);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(ProgramEventDetailContract.View view, String programId) {
        this.view = view;
        this.programId = programId;
        getProgram();
        getOrgUnits();
        getEvents(programId);
    }

    private void getEvents(String programId){
        Observable.just(programEventDetailRepository.programEvents(programId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> view.setData(data),
                        throwable -> Timber.e(throwable.getMessage())));
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


    @Override
    public void getOrgUnits() {
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

        compositeDisposable.add(metadataRepository.getProgramTrackedEntityAttributes(programId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::show,
                        Timber::d)
        );
    }

    private void show(List<ProgramTrackedEntityAttributeModel> programAttributes) {
        view.setAttributeOrder(programAttributes);
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
        currentCall.cancel();
        compositeDisposable.dispose();
    }
}

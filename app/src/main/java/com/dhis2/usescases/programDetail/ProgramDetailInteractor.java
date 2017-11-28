package com.dhis2.usescases.programDetail;

import android.support.annotation.NonNull;

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.user.UserRepository;
import com.dhis2.usescases.main.program.OrgUnitHolder;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import timber.log.Timber;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class ProgramDetailInteractor implements ProgramDetailContractModule.Interactor {

    ProgramDetailContractModule.View view;
    private D2 d2;
    private String ouMode = "DESCENDANTS";
    private String programId;
    private UserRepository userRepository;
    private ProgramRepository programRepository;
    private CompositeDisposable compositeDisposable;
    private ArrayList<OrganisationUnitModel> selectedOrgUnits = new ArrayList<>();
    private Call<TrackedEntityObject> currentCall;

    @Inject
    ProgramDetailInteractor(D2 d2, @NonNull UserRepository userRepository, @NonNull ProgramRepository programRepository) {
        this.d2 = d2;
        this.userRepository = userRepository;
        this.programRepository = programRepository;
        Bindings.setProgramRepository(programRepository);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(ProgramDetailContractModule.View view, String programId) {
        this.view = view;
        this.programId = programId;
        getOrgUnits();
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
        compositeDisposable.add(programRepository.programAttributes(programId)
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


    @Override
    public void getData(int page) {
        String orgQuey = "";
        for (int i = 0; i < selectedOrgUnits.size(); i++) {
            orgQuey = orgQuey.concat(selectedOrgUnits.get(i).uid());
            if (i < selectedOrgUnits.size() - 1)
                orgQuey = orgQuey.concat(";");
        }

        currentCall = d2.retrofit().create(TrackedEntityInstanceService.class).trackEntityInstances(
                orgQuey,
                ouMode,
                programId,
                true,
                page,
                "*,attributes[*],enrollments[enrollment,events[event,dueDate,programStage]]"
        );
        currentCall.enqueue(new Callback<TrackedEntityObject>() {
            @Override
            public void onResponse(Call<TrackedEntityObject> call, Response<TrackedEntityObject> response) {
                if (response.body() != null)
                    view.swapData(response.body());
            }

            @Override
            public void onFailure(Call<TrackedEntityObject> call, Throwable t) {

            }
        });

    }

    private void renderTree(List<OrganisationUnitModel> myOrgs) {

        selectedOrgUnits.addAll(myOrgs);

        getData(1);

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

    private interface TrackedEntityInstanceService {
        @GET("28/trackedEntityInstances")
        Call<TrackedEntityObject> trackEntityInstances(@Query("ou") String orgUnits,
                                                       @Query("ouMode") String ouMode,
                                                       @Query("program") String programId,
                                                       @Query("totalPages") boolean showPager,
                                                       @Query("page") int page,
                                                       @Query("fields") String fields);
    }
}

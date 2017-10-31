package com.dhis2.usescases.programDetail;

import android.support.annotation.NonNull;

import com.dhis2.data.user.UserRepository;
import com.dhis2.usescases.main.program.OrgUnitHolder;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

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
    private CompositeDisposable compositeDisposable;
    private ArrayList<OrganisationUnitModel> selectedOrgUnits = new ArrayList<>();

    @Inject
    ProgramDetailInteractor(D2 d2, @NonNull UserRepository userRepository) {
        this.d2 = d2;
        this.userRepository = userRepository;
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
                        this::renderTree,
                        Timber::d)
        );
    }

    @Override
    public void getData() {
        String orgQuey = "";
        for (int i = 0; i < selectedOrgUnits.size(); i++) {
            orgQuey = orgQuey.concat(selectedOrgUnits.get(i).uid());
            if (i < selectedOrgUnits.size() - 1)
                orgQuey = orgQuey.concat(",");
        }

        d2.retrofit().create(TrackedEntityInstanceService.class).trackEntityInstances(orgQuey, ouMode, programId).enqueue(new Callback<TrackedEntityObject>() {
            @Override
            public void onResponse(Call<TrackedEntityObject> call, Response<TrackedEntityObject> response) {
                view.swapData(response.body().getTrackedEntityInstances());
            }

            @Override
            public void onFailure(Call<TrackedEntityObject> call, Throwable t) {

            }
        });

    }

    private void renderTree(List<OrganisationUnitModel> myOrgs) {

        selectedOrgUnits.addAll(myOrgs);

        getData();

        List<OrganisationUnitModel> totalOrgs = new ArrayList<>();
        List<OrganisationUnitModel> toRemove = new ArrayList<>();

        totalOrgs.addAll(myOrgs);

        TreeNode root = TreeNode.root();

        int maxLevel = -1;
        int minLevel = 999;
        for (OrganisationUnitModel orgUnit : myOrgs) {
            maxLevel = orgUnit.level() > maxLevel ? orgUnit.level() : maxLevel;
            minLevel = orgUnit.level() < minLevel ? orgUnit.level() : minLevel;
        }

        ArrayList<TreeNode> treeNodes;
        for (int i = minLevel; i < maxLevel + 1; i++) {
            treeNodes = new ArrayList<>();
            for (OrganisationUnitModel orgUnit : myOrgs) {
                if (orgUnit.level() == i) {
                    treeNodes.add(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext())));
                }
            }

            if (i == minLevel)
                root.addChildren(treeNodes);

        }
/*
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
        }*/


        view.addTree(root);
    }

    private interface TrackedEntityInstanceService {
        @GET("28/trackedEntityInstances")
        Call<TrackedEntityObject> trackEntityInstances(@Query("ou") String orgUnits, @Query("ouMode") String ouMode, @Query("program") String programId);
    }
}

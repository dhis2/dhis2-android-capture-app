package com.dhis2.usescases.main;

import android.support.annotation.NonNull;

import com.dhis2.data.user.UserRepository;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.Payload;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.user.UserModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

public class MainInteractor implements MainContracts.Interactor {

    private D2 d2;
    private MainContracts.View view;
    private final UserRepository userRepository;
    private final CompositeDisposable compositeDisposable;

    MainInteractor(D2 d2, @NonNull UserRepository userRepository) {
        this.d2 = d2;
        this.userRepository = userRepository;
        this.compositeDisposable = new CompositeDisposable();

    }


    @Override
    public void init(MainContracts.View view) {

        this.view = view;

        ConnectableFlowable<UserModel> userObservable = userRepository.me()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .publish();

        /*ConnectableObservable<List<OrganisationUnitModel>> orgUnitObservable = userRepository.myOrgUnits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .publish();*/


        compositeDisposable.add(userObservable
                .map(this::username)
                .subscribe(
                        view.renderUsername(),
                        Timber::e));

        /*compositeDisposable.add(orgUnitObservable
                .subscribe(
                        this::getOrgUnits,
                        Timber::e));*/

        compositeDisposable.addAll(userObservable.connect()/*, orgUnitObservable.connect()*/);
    }

   /* @Override
    public void getOrgUnits(List<OrganisationUnitModel> orgs) {
        MyOrganisationUnitService orgUnitService = d2.retrofit().create(MyOrganisationUnitService.class);

        String[] uidPaths = orgs.get(0).path().split("/");

        orgUnitService.getOrganisationUnitChilds(uidPaths[1], "true", "id,level,parent,shortName").enqueue(new Callback<Payload<OrganisationUnit>>() {
            @Override
            public void onResponse(Call<Payload<OrganisationUnit>> call, Response<Payload<OrganisationUnit>> response) {
                renderTree(response.body().items(), *//*uidPaths,*//* orgs);
            }

            @Override
            public void onFailure(Call<Payload<OrganisationUnit>> call, Throwable t) {
                Timber.d(t);
            }
        });
    }*/



    /*private void renderTree(List<OrganisationUnit> organisationUnits, String[] uids, List<OrganisationUnitModel> orgs) {

        List<OrganisationUnit> totalOrgs = new ArrayList<>();
        totalOrgs.addAll(organisationUnits);

        TreeNode root = TreeNode.root();
        //LEVEL 1
        for (OrganisationUnit orgUnit : totalOrgs) {
            if (orgUnit.level() == 1) {
                if (uids[1].equals(orgUnit.uid()))
                    root.addChild(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext(), orgs)));
            }
        }

        //LEVEL 2
        for (TreeNode level1 : root.getChildren()) {
            for (OrganisationUnit orgUnit : totalOrgs) {
                if (orgUnit.level() == 2 && uids[2].equals(orgUnit.uid()) && ((OrganisationUnit) level1.getValue()).uid().equals(orgUnit.parent().uid())) {
                    level1.addChild(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext(), orgs)));
                }
            }
        }

        //LEVEL 3
        for (TreeNode level1 : root.getChildren()) {
            for (TreeNode level2 : level1.getChildren()) {
                for (OrganisationUnit orgUnit : totalOrgs) {
                    if (orgUnit.level() == 3 && uids[3].equals(orgUnit.uid()) && ((OrganisationUnit) level2.getValue()).uid().equals(orgUnit.parent().uid())) {
                        level2.addChild(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext(), orgs)));
                    }
                }
            }
        }

        //LEVEL 4
        for (TreeNode level1 : root.getChildren()) {
            for (TreeNode level2 : level1.getChildren()) {
                for (TreeNode level3 : level2.getChildren()) {
                    for (OrganisationUnit orgUnit : totalOrgs) {
                        if (orgUnit.level() == 4 && uids[4].equals(orgUnit.uid()) && ((OrganisationUnit) level3.getValue()).uid().equals(orgUnit.parent().uid())) {
                            level3.addChild(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext(), orgs)));
                        }
                    }
                }
            }
        }


        view.addTree(root);


    }*/

    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    private String username(@NonNull UserModel user) {
        String username = "";
        if (!isEmpty(user.firstName())) {
            username += user.firstName();
        }

        if (!isEmpty(user.surname())) {
            if (!username.isEmpty()) {
                username += " ";
            }

            username += user.surname();
        }

        return username;
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }


    public interface MyOrganisationUnitService {

        @GET("organisationUnits/{id}")
        Call<Payload<OrganisationUnit>> getOrganisationUnitChilds(
                @Path("id") String pathId,
                @Query("includeDescendants") String includeDescentants,
                @Query("fields") String fields
        );
    }


}
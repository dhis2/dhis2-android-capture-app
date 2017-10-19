package com.dhis2.usescases.main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.Gravity;

import com.dhis2.data.service.SyncService;
import com.dhis2.data.user.UserRepository;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.Payload;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.user.UserModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

final class MainPresenter implements MainContracts.Presenter {

    private MainContracts.View view;
    @Inject
    MainContracts.Interactor interactor;

    private final UserRepository userRepository;
    private final CompositeDisposable compositeDisposable;
    private final D2 d2;

    MainPresenter(@NonNull D2 d2,
                  @NonNull UserRepository userRepository,
                  MainContracts.Interactor interactor) {
        this.d2 = d2;
        this.userRepository = userRepository;
        this.compositeDisposable = new CompositeDisposable();
        this.interactor = interactor;
    }

    @Override
    public void init(MainContracts.View view) {
        this.view = view;
        sync();

        ConnectableFlowable<UserModel> userObservable = userRepository.me()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .publish();

        ConnectableObservable<List<OrganisationUnitModel>> orgUnitObservable = userRepository.myOrgUnits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .publish();


        compositeDisposable.add(userObservable
                .map(this::username)
                .subscribe(
                        view.renderUsername(),
                        Timber::e));

        compositeDisposable.add(orgUnitObservable
                .subscribe(
                        view.setOrgUnitTree(),
                        Timber::e));

       /* compositeDisposable.add(totalOrgUnit
                .subscribe(
                        organisationUnitPayload -> renderTree(organisationUnitPayload.items()),
                        Timber::e));*/


        compositeDisposable.addAll(userObservable.connect(), orgUnitObservable.connect());

        interactor.getOrgUnits().enqueue(new Callback<Payload<OrganisationUnit>>() {
            @Override
            public void onResponse(Call<Payload<OrganisationUnit>> call, Response<Payload<OrganisationUnit>> response) {
                renderTree(response.body().items());
            }

            @Override
            public void onFailure(Call<Payload<OrganisationUnit>> call, Throwable t) {
                Timber.d(t);
            }
        });

    }

    public void sync() {
        view.getContext().startService(new Intent(view.getContext().getApplicationContext(), SyncService.class));
    }

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
    public void logOut() {
        try {
            d2.logOut().call();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void blockSession() {

    }

    @Override
    public void onDetach() {
        compositeDisposable.clear();
    }

    @Override
    public void onMenuClick() {
        view.openDrawer(Gravity.START);
    }

    public void renderTree(List<OrganisationUnit> organisationUnits) {

        List<OrganisationUnit> totalOrgs = new ArrayList<>();
        totalOrgs.addAll(organisationUnits);

        TreeNode root = TreeNode.root();
        //LEVEL 1
        for (OrganisationUnit orgUnit : totalOrgs) {
            if (orgUnit.level() == 1)
                root.addChild(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext())));
        }
        //LEVEL 2
        for (TreeNode level1 : root.getChildren()) {
            for (OrganisationUnit orgUnit : totalOrgs) {
                if (orgUnit.level() == 2 && ((OrganisationUnit) level1.getValue()).uid().equals(orgUnit.parent().uid())) {
                    level1.addChild(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext())));
                }
            }
        }

        //LEVEL 3
        for (TreeNode level1 : root.getChildren()) {
            for (TreeNode level2 : level1.getChildren()) {
                for (OrganisationUnit orgUnit : totalOrgs) {
                    if (orgUnit.level() == 3 && ((OrganisationUnit) level2.getValue()).uid().equals(orgUnit.parent().uid())) {
                        level2.addChild(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext())));
                    }
                }
            }
        }

        //LEVEL 3
        for (TreeNode level1 : root.getChildren()) {
            for (TreeNode level2 : level1.getChildren()) {
                for (TreeNode level3 : level2.getChildren()) {
                    for (OrganisationUnit orgUnit : totalOrgs) {
                        if (orgUnit.level() == 4 && ((OrganisationUnit) level3.getValue()).uid().equals(orgUnit.parent().uid())) {
                            level3.addChild(new TreeNode(orgUnit).setViewHolder(new OrgUnitHolder(view.getContext())));
                        }
                    }
                }
            }
        }

        view.addTree(root);


    }
}
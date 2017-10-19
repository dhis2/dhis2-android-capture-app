package com.dhis2.usescases.main;


import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.dhis2.usescases.general.AbstractActivityContracts;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.common.Payload;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;

import io.reactivex.functions.Consumer;
import retrofit2.Call;

public final class MainContracts {

    interface View extends AbstractActivityContracts.View {

        @NonNull
        @UiThread
        Consumer<String> renderUsername();

        @NonNull
        @UiThread
        Consumer<String> renderUserInfo();

        @NonNull
        @UiThread
        Consumer<String> renderUserInitials();

        void openDrawer(int gravity);

        @NonNull
        @UiThread
        Consumer<List<OrganisationUnitModel>> setOrgUnitTree();

        @NonNull
        @UiThread
        Consumer<Payload<OrganisationUnit>> initOrgUnitTree();

        void addTree(TreeNode treeNode);

    }

    public interface Presenter {
        void init(View view);

        void onDetach();

        void onMenuClick();

        void sync();

        void logOut();

        void blockSession();
    }

    interface Interactor {
        Call<Payload<OrganisationUnit>> getOrgUnits();
    }

    interface Router {

    }

}
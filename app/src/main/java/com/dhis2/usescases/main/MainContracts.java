package com.dhis2.usescases.main;


import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.dhis2.usescases.general.AbstractActivityContracts;

import io.reactivex.functions.Consumer;

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

        void showHideFilter();

        void onLockClick(android.view.View view);

     /*   @NonNull
        @UiThread
        void addTree(TreeNode treeNode);*/

    }

    public interface Presenter {
        void init(View view);

        void onDetach();

        void onMenuClick();

        void sync();

        void logOut();

        void blockSession(String pin);

        void showFilter();
    }

    interface Interactor extends AbstractActivityContracts.Interactor {

        void init(View view);

    }

    interface Router {

    }

}
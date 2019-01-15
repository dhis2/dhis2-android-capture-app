package org.dhis2.usescases.main;


import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.ErrorMessageModel;

import java.util.List;

import io.reactivex.functions.Consumer;

public final class MainContracts {

    interface View extends AbstractActivityContracts.View {

        @NonNull
        @UiThread
        Consumer<String> renderUsername();

        void openDrawer(int gravity);

        void showHideFilter();

        void onLockClick();

        void changeFragment(int id);

        void showSyncErrors(List<ErrorMessageModel> data);
    }

    public interface Presenter {
        void init(View view);

        void onDetach();

        void onMenuClick();

        void logOut();

        void blockSession(String pin);

        void showFilter();

        void changeFragment(int id);

        void getErrors();
    }
}
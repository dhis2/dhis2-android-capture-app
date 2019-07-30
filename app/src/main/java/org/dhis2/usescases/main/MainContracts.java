package org.dhis2.usescases.main;


import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.hisp.dhis.android.core.maintenance.D2Error;

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

        void showSyncErrors(List<TrackerImportConflict> data);

        void updateFilters(int totalFilters);

        void showPeriodRequest(FilterManager.PeriodRequest periodRequest);
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
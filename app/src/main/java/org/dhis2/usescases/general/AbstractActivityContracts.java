package org.dhis2.usescases.general;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import org.dhis2.utils.OnDialogClickListener;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.granularsync.SyncStatusDialog;

/**
 * QUADRAM. Created by ppajuelo on 27/09/2017.
 */

public class AbstractActivityContracts {

    public interface View {
        Context getContext();

        ActivityGlobalAbstract getAbstracContext();

        ActivityGlobalAbstract getAbstractActivity();

        void startActivity(@NonNull Class<?> destination, @Nullable Bundle bundle, boolean finishCurrent, boolean finishAll, @Nullable ActivityOptionsCompat transition);

        void back();

        void displayMessage(String message);

        void showInfoDialog(String title, String message);

        void showInfoDialog(String title, String message, OnDialogClickListener dialogListener);

        void setTutorial();

        void showTutorial(boolean shaked);

        void hideKeyboard();

        void showToast(String message);

        void showInfoDialog(String title, String message, String possitiveButtonText, String negativeButtonText, OnDialogClickListener clickListener);

        void showDescription(String description);

        @Deprecated
        void showSyncDialog(SyncStatusDialog dialog);

        @Deprecated
        AnalyticsHelper analyticsHelper();
    }

    public interface Presenter {
        void onDettach();

        void displayMessage(String message);
    }

    public interface Interactor {
        void onDettach();
    }
}

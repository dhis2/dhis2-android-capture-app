package org.dhis2.usescases.general;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.appcompat.app.AlertDialog;

import org.dhis2.utils.OnDialogClickListener;

import java.util.List;

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
        AlertDialog showInfoDialog(String title, String message, OnDialogClickListener dialogListener);
        void setTutorial();

        void showTutorial(boolean shaked);

        <T> void saveListToPreference(String key, List<T> list);

        <T> List<T> getListFromPreference(String key);

        void hideKeyboard();

        void showToast(String message);

        void showDescription(String description);

        SharedPreferences getSharedPreferences();
    }

    public interface Presenter {
        void onDettach();

        void displayMessage(String message);
    }

    public interface Interactor {
        void onDettach();
    }
}

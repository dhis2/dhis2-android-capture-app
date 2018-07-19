package com.dhis2.usescases.general;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;

import java.util.List;

import me.toptas.fancyshowcase.FancyShowCaseQueue;

/**
 * Created by ppajuelo on 27/09/2017.
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
        void setTutorial();

        void showTutorial(boolean shaked);

        <T> void saveListToPreference(String key, List<T> list);

        <T> List<T> getListFromPreference(String key);

        void hideKeyboard();

        void showToast(String message);

        void showDescription(String description);
    }

    public interface Presenter {
        void onDettach();

        void displayMessage(String message);
    }

    public interface Interactor {
        void onDettach();
    }
}

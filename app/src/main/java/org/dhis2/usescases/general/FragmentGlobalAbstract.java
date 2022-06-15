package org.dhis2.usescases.general;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.dhis2.commons.locationprovider.LocationProvider;
import org.dhis2.utils.OnDialogClickListener;
import org.dhis2.utils.analytics.AnalyticsHelper;

import javax.inject.Inject;

/**
 * QUADRAM. Created by ppajuelo on 18/10/2017.
 */

public abstract class FragmentGlobalAbstract extends Fragment implements AbstractActivityContracts.View {

    @Inject
    public LocationProvider locationProvider;

    //region lifecycle

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    //endregion

    @Override
    public void startActivity(@NonNull Class<?> destination, @Nullable Bundle bundle, boolean finishCurrent, boolean finishAll, @Nullable ActivityOptionsCompat transition) {
        getAbstracContext().startActivity(destination, bundle, finishCurrent, finishAll, transition);
    }

    @Override
    public ActivityGlobalAbstract getAbstractActivity() {
        return (ActivityGlobalAbstract) getActivity();
    }

    @Override
    public void back() {
        getAbstracContext().back();
    }

    @Override
    public ActivityGlobalAbstract getAbstracContext() {
        return (ActivityGlobalAbstract) getActivity();
    }

    @Override
    public void hideKeyboard() {

    }

    @Override
    public void displayMessage(String message) {
        getAbstractActivity().displayMessage(message);
    }

    @Override
    public void showInfoDialog(String title, String message, String possitiveButtonText, String negativeButtonText, OnDialogClickListener clickListener) {
        getAbstractActivity().showInfoDialog(title, message, possitiveButtonText, negativeButtonText, clickListener);
    }

    @Override
    public void showInfoDialog(String title, String message) {
        getAbstractActivity().showInfoDialog(title, message);
    }

    @Override
    public void showInfoDialog(String title, String message, OnDialogClickListener clickListener) {
        getAbstractActivity().showInfoDialog(title, message, clickListener);
    }

    @Override
    public void setTutorial() {
    }

    @Override
    public void showTutorial(boolean shacked) {
    }

    @Override
    public void showDescription(String description) {
        getAbstractActivity().showDescription(description);
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return getAbstractActivity().getSharedPreferences();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSyncDialog(SyncStatusDialog dialog) {
        getAbstractActivity().showSyncDialog(dialog);
    }

    @Override
    public AnalyticsHelper analyticsHelper() {
        return getAbstractActivity().analyticsHelper();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (locationProvider != null) {
            locationProvider.stopLocationUpdates();
        }
    }
}

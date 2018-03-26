package com.dhis2.usescases.general;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.dhis2.R;
import com.dhis2.utils.Constants;
import com.dhis2.utils.CustomViews.CoordinatesView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by Javi on 28/07/2017.
 *
 */

public abstract class ActivityGlobalAbstract extends AppCompatActivity implements AbstractActivityContracts.View, CoordinatesView.OnMapPositionClick {

    private BehaviorSubject<Status> lifeCycleObservable = BehaviorSubject.create();

    public enum Status {
        ON_PAUSE,
        ON_RESUME
    }

    //****************
    //LIFECYCLE REGION

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Random r = new Random();

        switch (r.nextInt(3)) {
            case 0:
                setTheme(R.style.RedTheme);
                break;
            case 1:
                setTheme(R.style.GreenTheme);
                break;
            default:
                setTheme(R.style.AppTheme);
                break;
        }

        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        lifeCycleObservable.onNext(Status.ON_RESUME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        lifeCycleObservable.onNext(Status.ON_PAUSE);
    }


    //****************
    //PUBLIC METHOD REGION

    public Context getContext() {
        return this;
    }

    public ActivityGlobalAbstract getActivity() {
        return ActivityGlobalAbstract.this;
    }

    public void startActivity(@NonNull Class<?> destination, @Nullable Bundle bundle, boolean finishCurrent, boolean finishAll, @Nullable ActivityOptionsCompat transition) {
        if (finishCurrent)
            finish();
        Intent intent = new Intent(this, destination);
        if (bundle != null)
            intent.putExtras(bundle);
        if (transition != null)
            ContextCompat.startActivity(this, intent, transition.toBundle());
        else
            ContextCompat.startActivity(this, intent, null);
    }

    public ActivityGlobalAbstract getAbstracContext() {
        return this;
    }

    public ActivityGlobalAbstract getAbstractActivity() {
        return this;
    }

    public void back() {
        finish();
    }

    @Override
    public void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public <T> void saveListToPreference(String key, List<T> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);

        getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).edit().putString(key, json).apply();
    }

    @Override
    public <T> List<T> getListFromPreference(String key) {
        Gson gson = new Gson();
        String json = getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).getString(key, "[]");
        Type type = new TypeToken<List<T>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    public Observable<Status> observableLifeCycle() {
        return lifeCycleObservable;
    }

    public void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null)
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void renderError(String message) {
        if (getActivity() != null)
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(android.R.string.ok, null)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .show();
    }

    @Override
    public void onMapPositionClick(CoordinatesView coordinatesView) {
        coordinatesView.updateLocation(0, 0);
    }
}

package com.dhis2.usescases.general;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.dhis2.R;
import com.dhis2.usescases.login.LoginActivity;
import com.dhis2.usescases.main.MainActivity;
import com.dhis2.usescases.map.MapSelectorActivity;
import com.dhis2.usescases.splash.SplashActivity;
import com.dhis2.utils.ColorUtils;
import com.dhis2.utils.Constants;
import com.dhis2.utils.CustomViews.CoordinatesView;
import com.dhis2.utils.HelpManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * QUADRAM. Created by Javi on 28/07/2017.
 */

public abstract class ActivityGlobalAbstract extends AppCompatActivity implements AbstractActivityContracts.View, CoordinatesView.OnMapPositionClick {

    private BehaviorSubject<Status> lifeCycleObservable = BehaviorSubject.create();
    private CoordinatesView coordinatesView;

    public enum Status {
        ON_PAUSE,
        ON_RESUME
    }

    //****************
    //LIFECYCLE REGION

    public void setScreenName(String name) {
        Crashlytics.setString("SCREEN_NAME", name);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

       /* if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);TODO:ACTIVATE FOR FINAL BUILD*/
        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);
        if (this instanceof MainActivity || this instanceof LoginActivity || this instanceof SplashActivity) {
            prefs.edit().remove("PROGRAM_THEME").apply();
        }

        setTheme(prefs.getInt("PROGRAM_THEME", prefs.getInt("THEME", R.style.AppTheme)));

        Crashlytics.setString("SERVER", prefs.getString("SERVER", null));

        super.onCreate(savedInstanceState);

        Log.d("LIFECYCLE", getLocalClassName() + "-ON_CREATE");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LIFECYCLE", getLocalClassName() + "-ON_RESUME");
        lifeCycleObservable.onNext(Status.ON_RESUME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("LIFECYCLE", getLocalClassName() + "-ON_PAUSE");
        lifeCycleObservable.onNext(Status.ON_PAUSE);
    }

    @Override
    protected void onDestroy() {
        Log.d("LIFECYCLE", getLocalClassName() + "-ON_DESTROY");
        super.onDestroy();
    }

    //****************
    //PUBLIC METHOD REGION


    @Override
    public void setTutorial() {

    }

    @Override
    public void showTutorial(boolean shaked) {
        HelpManager.getInstance().showHelp();
    }

    public Context getContext() {
        return this;
    }

    public ActivityGlobalAbstract getActivity() {
        return ActivityGlobalAbstract.this;
    }

    public void startActivity(@NonNull Class<?> destination, @Nullable Bundle bundle, boolean finishCurrent, boolean finishAll, @Nullable ActivityOptionsCompat transition) {
        Intent intent = new Intent(this, destination);
        if (finishAll)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (bundle != null)
            intent.putExtras(bundle);
        if (transition != null)
            ContextCompat.startActivity(this, intent, transition.toBundle());
        else
            ContextCompat.startActivity(this, intent, null);
        if (finishCurrent)
            finish();
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
        if (message == null)
            message = getString(R.string.permission_denied);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast, findViewById(R.id.custom_toast_layout_id));
        ((TextView) layout.findViewById(R.id.toast_message)).setText(message);
        Toast toast = new Toast(this);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
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
        showInfoDialog(getString(R.string.error), message);
    }

    @Override
    public void showInfoDialog(String title, String message) {
        if (getActivity() != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();

            //TITLE
            final View titleView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_title, null);
            ((TextView) titleView.findViewById(R.id.dialogTitle)).setText(title);
            int colorPrimary = ColorUtils.getPrimaryColor(getActivity(), ColorUtils.ColorType.PRIMARY);
            alertDialog.setCustomTitle(titleView);

            //BODY
            final View msgView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_body, null);
            ((TextView) msgView.findViewById(R.id.dialogBody)).setText(message);
            msgView.findViewById(R.id.dialogAccept).setOnClickListener(view->alertDialog.dismiss());
            msgView.findViewById(R.id.dialogCancel).setOnClickListener(view->alertDialog.dismiss());
            alertDialog.setView(msgView);



            alertDialog.show();

        }
    }

    @Override
    public void onMapPositionClick(CoordinatesView coordinatesView) {
        this.coordinatesView = coordinatesView;
        startActivityForResult(MapSelectorActivity.create(this), Constants.RQ_MAP_LOCATION_VIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.RQ_MAP_LOCATION_VIEW:
                coordinatesView.updateLocation(Double.valueOf(data.getStringExtra(MapSelectorActivity.LATITUDE)), Double.valueOf(data.getStringExtra(MapSelectorActivity.LONGITUDE)));
                this.coordinatesView = null;
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showDescription(String description) {
        showInfoDialog("Description", description);
       /* LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast, findViewById(R.id.custom_toast_layout_id));
        ((TextView) layout.findViewById(R.id.toast_message)).setText(description);
        Toast toast = new Toast(this);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();*/
    }

    protected int getPrimaryColor() {
        TypedValue typedValue = new TypedValue();
        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }
}

package org.dhis2.usescases.general;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.App;
import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.usescases.login.LoginActivity;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.map.MapSelectorActivity;
import org.dhis2.usescases.splash.SplashActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.CustomViews.CoordinatesView;
import org.dhis2.utils.HelpManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    public ContentLoadingProgressBar progressBar;

    private BroadcastReceiver syncReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("action_sync")) {
                if (intent.getExtras() != null) {
                    boolean isMetaSync = intent.getExtras().getBoolean("metaSyncInProgress");
                    boolean isDataSync = intent.getExtras().getBoolean("dataSyncInProgress");
                    ((App) getApplication()).setMetaSync(isMetaSync);
                    ((App) getApplication()).seDataSync(isDataSync);

                    if (progressBar != null)
                        if (((App) getApplication()).isSyncing() && progressBar.getVisibility() == View.GONE)
                            progressBar.setVisibility(View.VISIBLE);
                        else if (!(((App) getApplication()).isSyncing()))
                            progressBar.setVisibility(View.GONE);
                }

            }
        }
    };

    public enum Status {
        ON_PAUSE,
        ON_RESUME
    }

    //****************
    //LIFECYCLE REGION

    public void setScreenName(String name) {
        Crashlytics.setString(Constants.SCREEN_NAME, name);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        if (!BuildConfig.DEBUG)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        SharedPreferences prefs = getSharedPreferences();
        if (this instanceof MainActivity || this instanceof LoginActivity || this instanceof SplashActivity) {
            prefs.edit().remove(Constants.PROGRAM_THEME).apply();
        }

        setTheme(prefs.getInt(Constants.PROGRAM_THEME, prefs.getInt(Constants.THEME, R.style.AppTheme)));

        Crashlytics.setString(Constants.SERVER, prefs.getString(Constants.SERVER, null));

        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(syncReceiver, new IntentFilter("action_sync"));
        lifeCycleObservable.onNext(Status.ON_RESUME);
        setProgressBar(findViewById(R.id.toolbarProgress));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(syncReceiver);
        super.onPause();
        lifeCycleObservable.onNext(Status.ON_PAUSE);
    }

    @Override
    protected void onDestroy() {
        progressBar = null;
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

    public void showMoreOptions(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.BOTTOM);
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        popupMenu.getMenuInflater().inflate(R.menu.home_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            showTutorial(false);
            return false;
        });
        popupMenu.show();
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
        String json = getSharedPreferences().getString(key, "[]");
        Type type = new TypeToken<List<T>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE);
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
            msgView.findViewById(R.id.dialogAccept).setOnClickListener(view -> alertDialog.dismiss());
            msgView.findViewById(R.id.dialogCancel).setOnClickListener(view -> alertDialog.dismiss());
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
                if (coordinatesView != null && resultCode == RESULT_OK && data.getExtras() != null) {
                    coordinatesView.updateLocation(Double.valueOf(data.getStringExtra(MapSelectorActivity.LATITUDE)), Double.valueOf(data.getStringExtra(MapSelectorActivity.LONGITUDE)));
                }
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

    public void setProgressBar(ContentLoadingProgressBar progressBar) {
        if (progressBar != null) {
            this.progressBar = progressBar;
            if (((App) getApplication()).isSyncing())
                progressBar.setVisibility(View.VISIBLE);
            else progressBar.setVisibility(View.GONE);
        }
    }
}

package org.dhis2.usescases.general;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.App;
import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.usescases.login.LoginActivity;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.utils.granular_sync.SyncStatusDialog;
import org.dhis2.usescases.map.MapSelectorActivity;
import org.dhis2.usescases.splash.SplashActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.OnDialogClickListener;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.custom_views.CoordinatesView;
import org.dhis2.utils.custom_views.CustomDialog;
import org.dhis2.utils.custom_views.PictureView;
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.processors.FlowableProcessor;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

/**
 * QUADRAM. Created by Javi on 28/07/2017.
 */

public abstract class ActivityGlobalAbstract extends AppCompatActivity
        implements AbstractActivityContracts.View, CoordinatesView.OnMapPositionClick,
        PictureView.OnIntentSelected {

    private BehaviorSubject<Status> lifeCycleObservable = BehaviorSubject.create();
    private CoordinatesView coordinatesView;
    public String uuid;
    @Inject
    public AnalyticsHelper analyticsHelper;

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
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (((App) getApplicationContext()).serverComponent() != null && analyticsHelper() != null)
            analyticsHelper().setD2(((App) getApplicationContext()).serverComponent().userManager().getD2());

        if (!getResources().getBoolean(R.bool.is_tablet))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        if (!BuildConfig.DEBUG && !BuildConfig.BUILD_TYPE.equals("beta"))
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        SharedPreferences prefs = getSharedPreferences();
        if (this instanceof MainActivity || this instanceof LoginActivity || this instanceof SplashActivity) {
            prefs.edit().remove(Constants.PROGRAM_THEME).apply();
        }

        if (!(this instanceof SplashActivity))
            setTheme(prefs.getInt(Constants.PROGRAM_THEME, prefs.getInt(Constants.THEME, R.style.AppTheme)));

        Crashlytics.setString(Constants.SERVER, prefs.getString(Constants.SERVER, null));
        String userName = prefs.getString(Constants.USER, null);
        if (userName != null)
            Crashlytics.setString(Constants.USER, userName);
        mFirebaseAnalytics.setUserId(prefs.getString(Constants.SERVER, null));

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

    @Override
    protected void onDestroy() {
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
            Timber.e(e);
        }
        popupMenu.getMenuInflater().inflate(R.menu.home_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            analyticsHelper.setEvent(SHOW_HELP, CLICK, SHOW_HELP);
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

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
    public AlertDialog showInfoDialog(String title, String message, OnDialogClickListener clickListener) {
        if (getActivity() != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();

            //TITLE
            final View titleView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_title, null);
            ((TextView) titleView.findViewById(R.id.dialogTitle)).setText(title);
            alertDialog.setCustomTitle(titleView);

            //BODY
            final View msgView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_body, null);
            ((TextView) msgView.findViewById(R.id.dialogBody)).setText(message);
            msgView.findViewById(R.id.dialogAccept).setOnClickListener(view -> {
                clickListener.onPossitiveClick(alertDialog);
                alertDialog.dismiss();
            });
            msgView.findViewById(R.id.dialogCancel).setOnClickListener(view -> {
                clickListener.onNegativeClick(alertDialog);
                alertDialog.dismiss();
            });
            alertDialog.setView(msgView);

            return alertDialog;

        } else
            return null;
    }

    @Override
    public AlertDialog showInfoDialog(String title, String message, String positiveButtonText, String negativeButtonText, OnDialogClickListener clickListener) {
        if (getActivity() != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();

            //TITLE
            final View titleView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_title, null);
            ((TextView) titleView.findViewById(R.id.dialogTitle)).setText(title);
            alertDialog.setCustomTitle(titleView);

            //BODY
            final View msgView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_body, null);
            ((TextView) msgView.findViewById(R.id.dialogBody)).setText(message);
            ((Button) msgView.findViewById(R.id.dialogAccept)).setText(positiveButtonText);
            ((Button) msgView.findViewById(R.id.dialogCancel)).setText(negativeButtonText);
            msgView.findViewById(R.id.dialogAccept).setOnClickListener(view -> {
                clickListener.onPossitiveClick(alertDialog);
                alertDialog.dismiss();
            });
            msgView.findViewById(R.id.dialogCancel).setOnClickListener(view -> {
                clickListener.onNegativeClick(alertDialog);
                alertDialog.dismiss();
            });
            alertDialog.setView(msgView);

            return alertDialog;

        } else
            return null;
    }

    @Override
    public void onMapPositionClick(CoordinatesView coordinatesView) {
        this.coordinatesView = coordinatesView;
        startActivityForResult(MapSelectorActivity.Companion.create(this,
                coordinatesView.getFeatureType(),
                coordinatesView.currentCoordinates()),
                Constants.RQ_MAP_LOCATION_VIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.RQ_MAP_LOCATION_VIEW) {
            if (coordinatesView != null && data.getExtras() != null) {
                FeatureType locationType = FeatureType.valueOf(data.getStringExtra(MapSelectorActivity.Companion.getLOCATION_TYPE_EXTRA()));
                String dataExtra = data.getStringExtra(MapSelectorActivity.Companion.getDATA_EXTRA());
                Geometry geometry;
                if (locationType == FeatureType.POINT) {
                    Type type = new TypeToken<List<Double>>() {
                    }.getType();
                    geometry = GeometryHelper.createPointGeometry(new Gson().fromJson(dataExtra, type));
                } else if (locationType == FeatureType.POLYGON) {
                    Type type = new TypeToken<List<List<List<Double>>>>() {
                    }.getType();
                    geometry = GeometryHelper.createPolygonGeometry(new Gson().fromJson(dataExtra, type));
                } else {
                    Type type = new TypeToken<List<List<List<List<Double>>>>>() {
                    }.getType();
                    geometry = GeometryHelper.createMultiPolygonGeometry(new Gson().fromJson(dataExtra, type));
                }
                coordinatesView.updateLocation(geometry);
            }
            this.coordinatesView = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showDescription(String description) {
        new CustomDialog(
                getAbstracContext(),
                getString(R.string.info),
                description,
                getString(R.string.action_close),
                null,
                Constants.DESCRIPTION_DIALOG,
                null
        ).show();
    }

    @Override
    public void showSyncDialog(String programUid, SyncStatusDialog.ConflictType conflictType, FlowableProcessor processor) {
        new SyncStatusDialog(programUid, conflictType, processor, analyticsHelper)
                .show(getSupportFragmentManager(), programUid);
    }

    @Override
    public void showSyncDialog(String orgUnit, String attributeCombo, String periodId,
                               SyncStatusDialog.ConflictType conflictType, FlowableProcessor processor, String dataSetUid) {
        new SyncStatusDialog(orgUnit, attributeCombo, periodId, conflictType, processor, analyticsHelper, dataSetUid)
                .show(getSupportFragmentManager(), attributeCombo);
    }

    @Override
    public void intentSelected(String uuid, Intent intent, int request, PictureView.OnPictureSelected onPictureSelected) {
        this.uuid = uuid;
        startActivityForResult(intent, request);
    }

    @Override
    public AnalyticsHelper analyticsHelper() {
        return analyticsHelper;
    }
}

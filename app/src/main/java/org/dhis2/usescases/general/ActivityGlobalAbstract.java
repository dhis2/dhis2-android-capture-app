package org.dhis2.usescases.general;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;
import static org.dhis2.utils.session.PinDialogKt.PIN_DIALOG_TAG;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.dhis2.App;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.R;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.dialogs.DialogClickListener;
import org.dhis2.commons.resources.LocaleSelector;
import org.dhis2.data.server.OpenIdSession;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.usescases.login.LoginActivity;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.splash.SplashActivity;
import org.dhis2.utils.ActivityResultObservable;
import org.dhis2.utils.ActivityResultObserver;
import org.dhis2.utils.Constants;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.OnDialogClickListener;
import org.dhis2.utils.analytics.AnalyticsConstants;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.dhis2.utils.reporting.CrashReportController;
import org.dhis2.utils.session.PinDialog;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.inject.Inject;

import kotlin.Unit;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;


public abstract class ActivityGlobalAbstract extends AppCompatActivity
        implements AbstractActivityContracts.View, ActivityResultObservable {

    private static final String FRAGMENT_TAG = "SYNC";

    private BehaviorSubject<Status> lifeCycleObservable = BehaviorSubject.create();
    public String uuid;
    @Inject
    public AnalyticsHelper analyticsHelper;
    @Inject
    public CrashReportController crashReportController;
    private PinDialog pinDialog;
    private boolean comesFromImageSource = false;

    private ActivityResultObserver activityResultObserver;

    public void requestEnableLocation() {
        displayMessage(getString(R.string.enable_location_message));
    }

    public enum Status {
        ON_PAUSE,
        ON_RESUME
    }


    public void setScreenName(String name) {
        crashReportController.trackScreenName(name);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        ServerComponent serverComponent = ((App) newBase.getApplicationContext()).getServerComponent();
        if (serverComponent != null && serverComponent.getD2().userModule().blockingIsLogged()) {
            ContextWrapper localeUpdatedContext = new LocaleSelector(newBase, serverComponent.getD2()).updateUiLanguage();
            super.attachBaseContext(localeUpdatedContext);
        } else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ServerComponent serverComponent = ((App) getApplicationContext()).getServerComponent();
        if (serverComponent != null) {
            serverComponent.openIdSession().setSessionCallback(this, logOutReason -> {
                startActivity(LoginActivity.class, LoginActivity.Companion.bundle(true, logOutReason), true, true, null);
                return Unit.INSTANCE;
            });
            boolean userIsLogged = serverComponent.userManager().isUserLoggedIn().blockingFirst();
            if (userIsLogged && !serverComponent.userManager().allowScreenShare()) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            } else if (!userIsLogged) {
                returnToLogin(false);
            }
        } else if (!(this instanceof SplashActivity) && !(this instanceof LoginActivity)) {
            returnToLogin(true);
        }

        if (!getResources().getBoolean(R.bool.is_tablet))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        SharedPreferences prefs = getSharedPreferences();
        if (this instanceof MainActivity || this instanceof LoginActivity || this instanceof SplashActivity) {
            prefs.edit().remove(Constants.PROGRAM_THEME).apply();
        }

        if (!(this instanceof SplashActivity) && !(this instanceof LoginActivity))
            setTheme(prefs.getInt(Constants.PROGRAM_THEME, prefs.getInt(Constants.THEME, R.style.AppTheme)));

        super.onCreate(savedInstanceState);
    }

    private void returnToLogin(boolean shouldRecreateServerComponent) {
        if (!(this instanceof SplashActivity) && !(this instanceof LoginActivity)) {
            if (shouldRecreateServerComponent) {
                ExtensionsKt.app(this).createServerComponent();
            }
            startActivity(LoginActivity.class, null, true, true, null);
        }
    }

    private void initPinDialog() {
        pinDialog = new PinDialog(PinDialog.Mode.ASK,
                (this instanceof LoginActivity),
                aBoolean -> {
                    startActivity(MainActivity.class, null, true, true, null);
                    return null;
                },
                () -> {
                    analyticsHelper.setEvent(AnalyticsConstants.FORGOT_CODE, AnalyticsConstants.CLICK, AnalyticsConstants.FORGOT_CODE);
                    if (!(this instanceof LoginActivity)) {
                        startActivity(LoginActivity.class, null, true, true, null);
                    }
                    return null;
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        lifeCycleObservable.onNext(Status.ON_RESUME);
        shouldCheckPIN();
    }

    private void shouldCheckPIN() {
        if (comesFromImageSource) {
            ExtensionsKt.app(this).disableBackGroundFlag();
            comesFromImageSource = false;
        } else {
            if (ExtensionsKt.app(this).isSessionBlocked() && !(this instanceof SplashActivity)) {
                if (getPinDialog() == null) {
                    initPinDialog();
                    showPinDialog();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        lifeCycleObservable.onNext(Status.ON_PAUSE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PinDialog dialog = getPinDialog();
        if (dialog != null) {
            dialog.dismissAllowingStateLoss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (activityResultObserver != null) {
            activityResultObserver.onRequestPermissionsResult(requestCode, permissions, grantResults);
            activityResultObserver = null;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void setTutorial() {

    }

    public void showPinDialog() {
        pinDialog.show(getSupportFragmentManager(), PIN_DIALOG_TAG);
    }

    public PinDialog getPinDialog() {
        return (PinDialog) getSupportFragmentManager().findFragmentByTag(PIN_DIALOG_TAG);
    }

    @Override
    public void showTutorial(boolean shaked) {
        if (HelpManager.getInstance().isReady()) {
            HelpManager.getInstance().showHelp();
        } else {
            showToast(getString(R.string.no_intructions));
        }
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
            showInfoDialog(title, message, new OnDialogClickListener() {
                @Override
                public void onPositiveClick() {

                }

                @Override
                public void onNegativeClick() {

                }
            });
        }
    }

    @Override
    public void showInfoDialog(String title, String message, OnDialogClickListener clickListener) {
        if (getActivity() != null) {
            showInfoDialog(title, message, getString(R.string.button_ok), getString(R.string.cancel), clickListener);
        }
    }

    @Override
    public void showInfoDialog(String title, String message, String positiveButtonText, String negativeButtonText, OnDialogClickListener clickListener) {
        if (getActivity() != null) {
            new MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
                    .setTitle(title)
                    .setCancelable(false)
                    .setMessage(message)
                    .setPositiveButton(positiveButtonText, (dialogInterface, i) -> clickListener.onPositiveClick())
                    .setNegativeButton(negativeButtonText, (dialogInterface, i) -> clickListener.onNegativeClick())
                    .show();
        }
    }

    @Override
    public void subscribe(@NotNull ActivityResultObserver activityResultObserver) {
        this.activityResultObserver = activityResultObserver;
    }

    @Override
    public void unsubscribe() {
        this.activityResultObserver = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (activityResultObserver != null) {
            comesFromImageSource = true;
            activityResultObserver.onActivityResult(requestCode, resultCode, data);
            activityResultObserver = null;
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
    public void showSyncDialog(SyncStatusDialog dialog) {
        dialog.show(getSupportFragmentManager(), FRAGMENT_TAG);
    }

    @Override
    public AnalyticsHelper analyticsHelper() {
        return analyticsHelper;
    }
}

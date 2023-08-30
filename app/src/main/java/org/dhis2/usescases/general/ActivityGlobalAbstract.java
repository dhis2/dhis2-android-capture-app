package org.dhis2.usescases.general;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;
import static org.dhis2.utils.session.PinDialogKt.PIN_DIALOG_TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.bindings.ExtensionsKt;
import org.dhis2.commons.ActivityResultObservable;
import org.dhis2.commons.ActivityResultObserver;
import org.dhis2.commons.Constants;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.locationprovider.LocationProvider;
import org.dhis2.commons.popupmenu.AppMenuHelper;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.usescases.login.LoginActivity;
import org.dhis2.usescases.login.accounts.AccountsActivity;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.usescases.qrScanner.ScanActivity;
import org.dhis2.usescases.splash.SplashActivity;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.OnDialogClickListener;
import org.dhis2.utils.analytics.AnalyticsConstants;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.dhis2.commons.reporting.CrashReportController;
import org.dhis2.utils.session.PinDialog;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import kotlin.Unit;


public abstract class ActivityGlobalAbstract extends AppCompatActivity
        implements AbstractActivityContracts.View, ActivityResultObservable {

    private static final String FRAGMENT_TAG = "SYNC";

    private BehaviorSubject<Status> lifeCycleObservable = BehaviorSubject.create();
    public String uuid;
    @Inject
    public AnalyticsHelper analyticsHelper;
    @Inject
    public CrashReportController crashReportController;
    @Inject
    public LocationProvider locationProvider;

    private PinDialog pinDialog;
    private boolean comesFromImageSource = false;

    private ActivityResultObserver activityResultObserver;
    private CustomDialog descriptionDialog;

    public enum Status {
        ON_PAUSE,
        ON_RESUME
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(
                ActivityGlobalAbstractExtensionsKt.wrappedContextForLanguage(
                        this,
                        ((App) newBase.getApplicationContext()).getServerComponent(),
                        newBase
                )
        );
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ServerComponent serverComponent = ((App) getApplicationContext()).getServerComponent();
        if (serverComponent != null) {
            serverComponent.openIdSession().setSessionCallback(this, logOutReason -> {
                startActivity(LoginActivity.class, LoginActivity.Companion.bundle(true, -1, false, logOutReason), true, true, null);
                return Unit.INSTANCE;
            });
            if (serverComponent.userManager().isUserLoggedIn().blockingFirst() &&
                    !serverComponent.userManager().allowScreenShare()) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            }
        }

        if (!getResources().getBoolean(R.bool.is_tablet))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        SharedPreferences prefs = getSharedPreferences();
        if (this instanceof MainActivity || this instanceof LoginActivity || this instanceof SplashActivity || this instanceof AccountsActivity) {
            if (serverComponent != null) {
                serverComponent.themeManager().clearProgramTheme();
            }
            prefs.edit().remove(Constants.PROGRAM_THEME).apply();
        }

        if (!(this instanceof SplashActivity) &&
                !(this instanceof LoginActivity) &&
                !(this instanceof AccountsActivity) &&
                !(this instanceof ScanActivity)
        ) {
            if (serverComponent != null) {
                setTheme(serverComponent.themeManager().getProgramTheme());
            } else {
                setTheme(R.style.AppTheme);
            }
        }

        super.onCreate(savedInstanceState);
    }

    private void initPinDialog() {
        pinDialog = new PinDialog(PinDialog.Mode.ASK,
                (this instanceof LoginActivity),
                () -> {
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
        if (locationProvider != null) {
            locationProvider.stopLocationUpdates();
        }
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
        new AppMenuHelper.Builder()
                .menu(this, R.menu.home_menu)
                .anchor(view)
                .onMenuInflated(popupMenu -> {
                    return Unit.INSTANCE;
                })
                .onMenuItemClicked(item -> {
                    analyticsHelper.setEvent(SHOW_HELP, CLICK, SHOW_HELP);
                    showTutorial(false);
                    return false;
                })
                .build()
                .show();
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
        if (descriptionDialog != null) {
            descriptionDialog.cancel();
        }
        descriptionDialog = new CustomDialog(
                getAbstracContext(),
                getString(R.string.info),
                description,
                getString(R.string.action_close),
                null,
                Constants.DESCRIPTION_DIALOG,
                null
        );

        descriptionDialog.show();
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

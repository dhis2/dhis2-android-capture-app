package org.dhis2.usescases.general;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.commons.ActivityResultObservable;
import org.dhis2.commons.Constants;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.popupmenu.AppMenuHelper;
import org.dhis2.mobile.commons.reporting.CrashReportController;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.OnDialogClickListener;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.granularsync.SyncStatusDialog;

import javax.inject.Inject;

import kotlin.Unit;


public abstract class ActivityGlobalAbstract extends SessionManagerActivity
        implements AbstractActivityContracts.View, ActivityResultObservable {

    private static final String FRAGMENT_TAG = "SYNC";

    public String uuid;

    @Inject
    public CrashReportController crashReportController;

    private CustomDialog descriptionDialog;


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
    public void setTutorial() {

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
                    // no-op
                }

                @Override
                public void onNegativeClick() {
                    // no-op
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

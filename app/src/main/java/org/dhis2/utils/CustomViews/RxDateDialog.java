package org.dhis2.utils.CustomViews;

import android.app.AlertDialog;

import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Period;

import java.util.Date;
import java.util.List;

import io.reactivex.SingleEmitter;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by ppajuelo on 15/01/2018.
 */

public class RxDateDialog {
    public static int BUTTON_POSSITIVE = AlertDialog.BUTTON_POSITIVE;
    public static int BUTTON_NEGATIVE = AlertDialog.BUTTON_NEGATIVE;

    private final ActionTrigger<DateDialog> actionTrigger = ActionTrigger.create();
    private final CompositeDisposable compositeSubscription = new CompositeDisposable();
    private final ActivityGlobalAbstract activity;
    private final Period period;

    public RxDateDialog(final ActivityGlobalAbstract activity, Period mPeriod) {
        this.activity = activity;
        this.period = mPeriod;

        activity.observableLifeCycle().subscribe(activityStatus -> {
            if (activityStatus == ActivityGlobalAbstract.Status.ON_RESUME)
                compositeSubscription.add(actionTrigger.observe().subscribe(this::showDialog));
            else
                compositeSubscription.clear();
        });

    }

    private void showDialog(DateDialog dialog) {
        dialog.setCancelable(true);
        dialog.setPossitiveListener(v -> {
            notifyClick(dialog.callback, dialog.getFilters());
            dialog.dismiss();
        });
        dialog.setNegativeListener(v -> {
            notifyClick(dialog.callback, dialog.clearFilters());
            dialog.dismiss();
        });

        if(activity.getSupportFragmentManager().findFragmentByTag("dialog") == null){
            activity.getSupportFragmentManager().beginTransaction().add(dialog, "dialog").commit();
        }


    }

    public DateDialog create() {
        return DateDialog.newInstace(actionTrigger, period);
    }

    private void notifyClick(SingleEmitter<List<Date>> callback, List<Date> button) {
        callback.onSuccess(button);
    }

}

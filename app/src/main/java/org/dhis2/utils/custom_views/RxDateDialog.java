package org.dhis2.utils.custom_views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.SingleEmitter;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 15/01/2018.
 */

public class RxDateDialog {
    private final ActionTrigger<DateDialog> actionTrigger = ActionTrigger.create();
    private final CompositeDisposable compositeSubscription = new CompositeDisposable();
    private final ActivityGlobalAbstract activity;
    private Period period;
    private Map<String, String> mapPeriods;

    private boolean isDataSet = false;
    @SuppressLint({"RxLeakedSubscription", "RxSubscribeOnError"})
    public RxDateDialog(final ActivityGlobalAbstract activity, Period mPeriod) {
        this.activity = activity;
        this.period = mPeriod;

        activity.observableLifeCycle().subscribe(activityStatus -> {
            if (activityStatus == ActivityGlobalAbstract.Status.ON_RESUME)
                compositeSubscription.add(actionTrigger.observe().subscribe(this::showDialog, Timber::d));
            else
                compositeSubscription.clear();
        });

    }

    @SuppressLint({"RxLeakedSubscription", "RxSubscribeOnError"})
    public RxDateDialog(final ActivityGlobalAbstract activity, Map<String, String> mapPeriods, boolean isDataSet) {
        this.activity = activity;
        this.mapPeriods = mapPeriods;
        this.isDataSet = isDataSet;

        activity.observableLifeCycle().subscribe(activityStatus -> {
            if (activityStatus == ActivityGlobalAbstract.Status.ON_RESUME)
                compositeSubscription.add(actionTrigger.observe().subscribe(this::showDialog, Timber::d));
            else
                compositeSubscription.clear();
        }, Timber::e);

    }

    private void showDialog(DateDialog dialog) {
        dialog.setCancelable(true);
        dialog.setPossitiveListener(v -> {
            if(mapPeriods == null) notifyClick(dialog.callback, dialog.getFilters());
            else notifyClickDataSet(dialog.callbackPeriod, dialog.getFiltersPeriod());
            dialog.dismiss();
        });
        dialog.setNegativeListener(v -> {
            if(mapPeriods == null) notifyClick(dialog.callback, dialog.clearFilters());
            else notifyClickDataSet(dialog.callbackPeriod, dialog.clearFiltersPeriod());
            dialog.dismiss();
        });

        if(activity.getSupportFragmentManager().findFragmentByTag("dialog") == null){
            activity.getSupportFragmentManager().beginTransaction().add(dialog, "dialog").commit();
        }


    }

    public DateDialog create() {
        if(isDataSet)
            return DateDialog.newInstace(actionTrigger, mapPeriods);
        return DateDialog.newInstace(actionTrigger, period);
    }

    public DateDialog createForFilter(){
        return DateDialog.newInstace(actionTrigger, activity);
    }

    private void notifyClick(SingleEmitter<Pair<Period, List<Date>>> callback, Pair<Period, List<Date>> button) {
        callback.onSuccess(button);
    }

    private void notifyClickDataSet(SingleEmitter<List<String>> callback, List<String> button) {
        callback.onSuccess(button);
    }

}

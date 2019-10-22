package org.dhis2.utils.customviews;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.DialogDateBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Period;
import org.dhis2.utils.filters.FilterManager;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;

/**
 * QUADRAM. Created by ppajuelo on 05/12/2017.
 */

public class DateDialog extends DialogFragment {

    private DialogDateBinding binding;
    private static ActionTrigger<DateDialog> dialogActionTrigger;
    protected SingleEmitter<Pair<Period, List<Date>>> callback;
    protected SingleEmitter<List<String>> callbackPeriod;
    private static DateDialog instace;
    private static Period period = Period.WEEKLY;
    private static DateAdapter adapter;

    private View.OnClickListener possitiveListener;
    private View.OnClickListener negativeListener;
    private static ActivityGlobalAbstract activity;

    public static DateDialog newInstace(ActionTrigger<DateDialog> mActionTrigger, Period mPeriod) {
        if (period != mPeriod || instace == null) {
            period = mPeriod;
            dialogActionTrigger = mActionTrigger;

            instace = new DateDialog();
            adapter = new DateAdapter(period);
        }
        return instace;
    }

    public static DateDialog newInstace(ActionTrigger<DateDialog> mActionTrigger, Map<String, String> mapPeriods) {
        dialogActionTrigger = mActionTrigger;
        instace = new DateDialog();
        adapter = new DateAdapter();
        adapter.swapMapPeriod(mapPeriods);
        return instace;
    }

    public static DateDialog newInstace(ActionTrigger<DateDialog> mActionTrigger, ActivityGlobalAbstract activityGlobal) {
        period = Period.WEEKLY;
        dialogActionTrigger = mActionTrigger;
        activity = activityGlobal;

        instace = new DateDialog();
        adapter = new DateAdapter(period);
        return instace;
    }

    public DateDialog() {
        // do nothing
    }


    public DateDialog setPossitiveListener(View.OnClickListener listener) {
        this.possitiveListener = listener;
        return this;
    }

    public DateDialog setNegativeListener(View.OnClickListener listener) {
        this.negativeListener = listener;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_date, container, false);

        binding.recyclerDate.setAdapter(adapter);

        binding.setTitleText(getString(period.getNameResouce()));

        binding.acceptButton.setOnClickListener(possitiveListener);
        binding.clearButton.setOnClickListener(negativeListener);

        binding.nextPeriod.setOnClickListener(v -> manageButtonPeriods(true));
        binding.previousPeriod.setOnClickListener(v -> manageButtonPeriods(false));
        return binding.getRoot();
    }

    private void manageButtonPeriods(boolean next){
        Period period = adapter.swapPeriod(next);
        if(period == Period.DAILY) {
            DateUtils.getInstance().showPeriodDialog(activity, datePeriods ->
                    FilterManager.getInstance().addPeriod(
                            null
                    ), true);
            dismiss();
        }

        binding.setTitleText(getString(period.getNameResouce()));
    }

    private DateDialog withEmitter(final SingleEmitter<Pair<Period, List<Date>>> emitter) {
        this.callback = emitter;
        return this;
    }

    private DateDialog withEmitterSelectedPeriod(final SingleEmitter<List<String>> emitter) {
        this.callbackPeriod = emitter;
        return this;
    }

    public Single<Pair<Period, List<Date>>> show() {
        return Single.create(emitter -> dialogActionTrigger.trigger(withEmitter(emitter)));
    }

    public Single<List<String>> showSelectedPeriod() {
        return Single.create(emitter -> dialogActionTrigger.trigger(withEmitterSelectedPeriod(emitter)));
    }

    public Pair<Period, List<Date>> getFilters() {
        return adapter.getSelectedDates();
    }

    public List<String> getFiltersPeriod() {
        return adapter.getSeletedDatesName();
    }

    public Pair<Period, List<Date>> clearFilters() {
        return adapter.clearFilters();
    }

    public List<String> clearFiltersPeriod() {
        return adapter.clearFiltersPeriod();
    }
}

package org.dhis2.utils.custom_views;

import android.app.Dialog;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import org.dhis2.R;
import org.dhis2.databinding.DialogDateBinding;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;

/**
 * QUADRAM. Created by ppajuelo on 05/12/2017.
 */

public class DateDialog extends DialogFragment {

    private static ActionTrigger<DateDialog> dialogActionTrigger;
    protected SingleEmitter<List<Date>> callback;
    protected SingleEmitter<List<String>> callbackPeriod;
    private static DateDialog instace;
    private static Period period = Period.WEEKLY;
    private static DateAdapter adapter;

    private View.OnClickListener possitiveListener;
    private View.OnClickListener negativeListener;

    public static DateDialog newInstace(Period mPeriod) {
        if (period != mPeriod || instace == null) {
            period = mPeriod;
            instace = new DateDialog();
            adapter = new DateAdapter(period);
        }
        return instace;
    }

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
        if (instace == null) {
            dialogActionTrigger = mActionTrigger;
            instace = new DateDialog();
            adapter = new DateAdapter();
        }

        adapter.swapMapPeriod(mapPeriods);
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        DialogDateBinding binding = DataBindingUtil.inflate(inflater, R.layout.dialog_date, container, false);

        binding.recyclerDate.setAdapter(adapter);

        binding.setTitleText(getString(R.string.date_dialog_tigle));

        binding.acceptButton.setOnClickListener(possitiveListener);
        binding.acceptButton.setOnClickListener(possitiveListener);
        binding.clearButton.setOnClickListener(negativeListener);


        return binding.getRoot();
    }

    private DateDialog withEmitter(final SingleEmitter<List<Date>> emitter) {
        this.callback = emitter;
        return this;
    }

    private DateDialog withEmitterSelectedPeriod(final SingleEmitter<List<String>> emitter) {
        this.callbackPeriod = emitter;
        return this;
    }

    public Single<List<Date>> show() {
        return Single.create(emitter -> dialogActionTrigger.trigger(withEmitter(emitter)));
    }

    public Single<List<String>> showSelectedPeriod() {
        return Single.create(emitter -> dialogActionTrigger.trigger(withEmitterSelectedPeriod(emitter)));
    }

    public List<Date> getFilters() {
        return adapter.getSelectedDates();
    }

    public List<String> getFiltersPeriod(){
        return adapter.getSeletedDatesName();
    }

    public List<Date> clearFilters() {
        return adapter.clearFilters();
    }

    public List<String> clearFiltersPeriod(){
        return adapter.clearFiltersPeriod();
    }
}

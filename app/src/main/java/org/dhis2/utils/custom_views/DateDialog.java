package org.dhis2.utils.custom_views;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import org.dhis2.R;
import org.dhis2.databinding.DialogDateBinding;
import org.dhis2.utils.Period;

import java.util.Date;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;

/**
 * QUADRAM. Created by ppajuelo on 05/12/2017.
 */

public class DateDialog extends DialogFragment {

    static private ActionTrigger<DateDialog> dialogActionTrigger;
    public SingleEmitter<List<Date>> callback;

    static DateDialog instace;
    static Period period = Period.WEEKLY;
    static DateAdapter adapter;

    View.OnClickListener possitiveListener;
    View.OnClickListener negativeListener;

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

    public DateDialog() {

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

    public Single<List<Date>> show() {
        return Single.create(emitter -> dialogActionTrigger.trigger(withEmitter(emitter)));
    }

    public List<Date> getFilters() {
        return adapter.getSelectedDates();
    }

    public List<Date> clearFilters() {
        return adapter.clearFilters();
    }
}

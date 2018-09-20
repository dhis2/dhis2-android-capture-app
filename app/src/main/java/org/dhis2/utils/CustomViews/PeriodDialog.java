package org.dhis2.utils.CustomViews;

import android.app.Dialog;
import android.content.Context;
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
import org.dhis2.databinding.DialogPeriodBinding;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.Calendar;
import java.util.Date;

/**
 * QUADRAM. Created by ppajuelo on 21/05/2018.
 */

public class PeriodDialog extends DialogFragment {
    DialogPeriodBinding binding;
    private OnDateSet possitiveListener;
    private View.OnClickListener negativeListener;
    private String title;

    private Context context;
    private Date currentDate;
    private PeriodType period;
    private Date minDate;


    public PeriodDialog() {
        possitiveListener = null;
        negativeListener = null;
        title = null;
        currentDate = Calendar.getInstance().getTime();
    }

    public PeriodDialog setPeriod(PeriodType period) {
        this.period = period;
        return this;
    }

    public PeriodDialog setPossitiveListener(OnDateSet listener) {
        this.possitiveListener = listener;
        return this;
    }

    public PeriodDialog setNegativeListener(View.OnClickListener listener) {
        this.negativeListener = listener;
        return this;
    }


    public PeriodDialog setTitle(String title) {
        this.title = title;
        return this;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_period, container, false);

        binding.title.setText(title);
        binding.acceptButton.setOnClickListener(view -> {
            if (minDate != null && minDate.equals(currentDate))
                binding.periodBefore.setEnabled(false);
            else
                binding.periodBefore.setEnabled(true);

            possitiveListener.onDateSet(currentDate);
        });
        binding.clearButton.setOnClickListener(view -> dismiss());

        binding.periodSubtitle.setText(period.name());
        if (minDate == null || currentDate.after(minDate))
            currentDate = DateUtils.getInstance().getNextPeriod(period, currentDate, 0);
        else if(minDate != null && currentDate.before(minDate))
            currentDate = DateUtils.getInstance().getNextPeriod(period,minDate,0);
        else
            currentDate = DateUtils.getInstance().getNextPeriod(period, currentDate, 0);

        binding.selectedPeriod.setText(DateUtils.uiDateFormat().format(currentDate));

        binding.periodBefore.setOnClickListener(view -> previousPeriod());
        binding.periodNext.setOnClickListener(view -> nextPeriod());

        return binding.getRoot();
    }

    public void nextPeriod() {
        currentDate = DateUtils.getInstance().getNextPeriod(period, currentDate, 1);
        binding.selectedPeriod.setText(DateUtils.uiDateFormat().format(currentDate));
    }

    public void previousPeriod() {
        currentDate = DateUtils.getInstance().getNextPeriod(period, currentDate, -1);
        binding.selectedPeriod.setText(DateUtils.uiDateFormat().format(currentDate));
    }

    public PeriodDialog setMinDate(Date minDate) {
        this.minDate = minDate;
        return this;
    }

    public interface OnDateSet {
        void onDateSet(Date selectedDate);
    }
}

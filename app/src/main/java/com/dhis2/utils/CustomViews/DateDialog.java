package com.dhis2.utils.CustomViews;

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

import com.dhis2.R;
import com.dhis2.databinding.DialogDateBinding;

/**
 * Created by ppajuelo on 05/12/2017.
 */

public class DateDialog extends DialogFragment {

    static DateDialog instace;
    static DateAdapter.Period period = DateAdapter.Period.WEEKLY;
    static DateAdapter adapter;
    public static DateDialog newInstace(DateAdapter.Period mPeriod) {
        if (period != mPeriod || instace == null) {
            period = mPeriod;
            instace = new DateDialog();
            adapter = new DateAdapter(period);
        }
        return instace;
    }

    public DateDialog() {

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

        binding.setTitleText("Select dates to filter");

        binding.acceptButton.setOnClickListener(view -> {
            dismiss();
        });


        return binding.getRoot();
    }
}

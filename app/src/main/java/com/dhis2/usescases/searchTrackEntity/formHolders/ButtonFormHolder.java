package com.dhis2.usescases.searchTrackEntity.formHolders;

import android.app.DatePickerDialog;
import android.databinding.ViewDataBinding;
import android.widget.DatePicker;

import com.dhis2.BR;
import com.dhis2.databinding.FormButtonTextBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.Locale;

/**
 * Created by ppajuelo on 06/11/2017.
 */

public class ButtonFormHolder extends FormViewHolder implements DatePickerDialog.OnDateSetListener {

    SearchTEContractsModule.Presenter presenter;
    TrackedEntityAttributeModel bindableOnject;

    public ButtonFormHolder(ViewDataBinding binding) {
        super(binding);
    }

    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableOnject) {
        this.presenter = presenter;
        this.bindableOnject = bindableOnject;

        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.attribute, bindableOnject);
        binding.executePendingBindings();

        ((FormButtonTextBinding) binding).buttonDate.setOnFocusChangeListener((view, b) -> {
            if (b)
                presenter.onDateClick(ButtonFormHolder.this);
        });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        String date = String.format(Locale.getDefault(), "%s-%02d-%02d", year, month + 1, day);
        ((FormButtonTextBinding) binding).buttonDate.setText(date);
        ((FormButtonTextBinding) binding).buttonDate.clearFocus();

        presenter.query(String.format("%s:EQ:%s", bindableOnject.uid(), date));

    }
}

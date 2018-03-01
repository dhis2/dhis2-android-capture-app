package com.dhis2.data.forms.dataentry.fields;

import android.app.DatePickerDialog;
import android.databinding.ViewDataBinding;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.DatePicker;

import com.dhis2.BR;
import com.dhis2.databinding.FormButtonTextBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.Locale;

/**
 * Created by ppajuelo on 06/11/2017.
 */

public class ButtonFormHolder extends FormViewHolder
        implements DatePickerDialog.OnDateSetListener,
        LocationListener {

    SearchTEContractsModule.Presenter presenter;
    TrackedEntityAttributeModel bindableOnject;
    int programPosition;

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
            if (b) {
                presenter.onDateClick(ButtonFormHolder.this);
            }
        });
    }

    public void bindProgramData(SearchTEContractsModule.Presenter presenter, String label, int position) {
        this.presenter = presenter;
        this.programPosition = position;
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.label, label);
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

        if (bindableOnject != null)
            presenter.query(String.format("%s:GT:%s", bindableOnject.uid(), date), true);
        else
            presenter.query(programPosition == 0 ? "programEnrollmentStartDate=" + date : "programIncidentStartDate=" + date, false);


    }

    @Override
    public void onLocationChanged(Location location) {
        String coordinates = String.format(Locale.getDefault(), "%s, %s", location.getLatitude(), location.getLongitude());
        ((FormButtonTextBinding) binding).buttonDate.setText(coordinates);
        ((FormButtonTextBinding) binding).buttonDate.clearFocus();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}

package com.dhis2.usescases.searchTrackEntity.formHolders;

import android.app.DatePickerDialog;
import android.databinding.ViewDataBinding;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.dhis2.BR;
import com.dhis2.R;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

/**
 * Created by ppajuelo on 06/11/2017.
 */

public class ButtonFormHolder extends FormViewHolder implements DatePickerDialog.OnDateSetListener {


    public ButtonFormHolder(ViewDataBinding binding) {
        super(binding);
        this.binding = binding;
    }

    @Override
    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableOnject) {


    }

    public void bindData(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableOnject) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.attribute, bindableOnject);

        /*if (bindableOnject.valueType() == ValueType.DATE)
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.onDateClick(ButtonFormHolder.this);
                }
            });*/

        binding.executePendingBindings();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        String date = String.format("%s-%s-%s", i, i1 + 1, i2);
        ((Button) binding.getRoot().findViewById(R.id.button_date)).setText(date);
    }
}

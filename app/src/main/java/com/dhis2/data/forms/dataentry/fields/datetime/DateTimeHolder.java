package com.dhis2.data.forms.dataentry.fields.datetime;

import android.databinding.ViewDataBinding;

import com.dhis2.BR;
import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.jakewharton.rxbinding2.view.RxView;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import io.reactivex.processors.FlowableProcessor;


/**
 * Created by frodriguez on 16/01/2018.
 */

public class DateTimeHolder extends FormViewHolder {

    SearchTEContractsModule.Presenter presenter;
    TrackedEntityAttributeModel bindableObject;

    public DateTimeHolder(ViewDataBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
    }

    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableObject) {
        this.presenter = presenter;
        this.bindableObject = bindableObject;
        binding.setVariable(BR.attribute, bindableObject);
        binding.executePendingBindings();

    }

    public void bindProgramData(SearchTEContractsModule.Presenter presenter, String label, int position) {
        this.presenter = presenter;
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.label, label);
        binding.executePendingBindings();

    }

}

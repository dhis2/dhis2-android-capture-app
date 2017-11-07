package com.dhis2.usescases.searchTrackEntity.formHolders;

import android.databinding.ViewDataBinding;
import android.util.Log;
import android.view.View;

import com.dhis2.BR;
import com.dhis2.databinding.FormEditTextBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

/**
 * Created by ppajuelo on 07/11/2017.
 */

public class EditTextHolder extends FormViewHolder {

    public EditTextHolder(ViewDataBinding binding) {
        super(binding);
    }

    @Override
    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableOnject) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.attribute, bindableOnject);

        binding.executePendingBindings();

        ((FormEditTextBinding) binding).formEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b)
                    Log.d("FOCUS", "FOCUS!");
            }
        });

    }
}

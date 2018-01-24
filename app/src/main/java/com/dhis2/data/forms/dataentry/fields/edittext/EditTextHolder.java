package com.dhis2.data.forms.dataentry.fields.edittext;

import android.databinding.ViewDataBinding;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;

import com.dhis2.BR;
import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
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

        int inputType;
        switch (bindableOnject.valueType()) {
            case AGE:
            case NUMBER:
            case INTEGER:
            case PERCENTAGE:
            case INTEGER_NEGATIVE:
            case INTEGER_POSITIVE:
            case INTEGER_ZERO_OR_POSITIVE:
                inputType = InputType.TYPE_CLASS_NUMBER;
                break;
            case EMAIL:
                inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                break;
            case PHONE_NUMBER:
                inputType = InputType.TYPE_CLASS_PHONE;
                break;
            default:
                inputType = InputType.TYPE_CLASS_TEXT;
                break;
        }
        ((FormEditTextBinding) binding).formEdittext.setInputType(inputType);

        ((FormEditTextBinding) binding).formEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                presenter.query(String.format("%s:LIKE:%s", bindableOnject.uid(), charSequence), true); //Searchs for attributes which contains charSequece in its value
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }


}

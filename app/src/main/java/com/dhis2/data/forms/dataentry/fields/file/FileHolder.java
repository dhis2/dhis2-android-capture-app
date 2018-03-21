package com.dhis2.data.forms.dataentry.fields.file;

import android.databinding.ViewDataBinding;

import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

/**
 * Created by ppajuelo on 19/03/2018.
 */

public class FileHolder extends FormViewHolder {

    public FileHolder(ViewDataBinding binding) {
        super(binding);
    }

    @Override
    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableOnject) {

    }
}

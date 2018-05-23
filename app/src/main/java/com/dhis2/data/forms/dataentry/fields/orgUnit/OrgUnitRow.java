package com.dhis2.data.forms.dataentry.fields.orgUnit;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormButtonBinding;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.processors.FlowableProcessor;

/**
 * Created by ppajuelo on 19/03/2018.
 */

public class OrgUnitRow implements Row<OrgUnitHolder, OrgUnitViewModel> {

    private final boolean isBgTransparent;
    private final FlowableProcessor<RowAction> processor;
    private final LayoutInflater inflater;
    private final FragmentManager fm;
    private final Observable<List<OrganisationUnitModel>> orgUnits;
    private final String renderType;
    private FormButtonBinding binding;

    public OrgUnitRow(FragmentManager fm, LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor,
                      boolean isBgTransparent, Observable<List<OrganisationUnitModel>> orgUnits) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.fm = fm;
        this.orgUnits = orgUnits;
        this.renderType = null;
    }

    public OrgUnitRow(FragmentManager fm, LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor,
                      boolean isBgTransparent, Observable<List<OrganisationUnitModel>> orgUnits, String renderType) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.fm = fm;
        this.orgUnits = orgUnits;
        this.renderType = renderType;
    }

    @NonNull
    @Override
    public OrgUnitHolder onCreate(@NonNull ViewGroup parent) {
        binding = DataBindingUtil.inflate(inflater, R.layout.form_button, parent, false);
        if (isBgTransparent)
            binding.formButton.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.colorPrimary));
        else
            binding.formButton.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.colorAccent));
        return new OrgUnitHolder(fm, binding, processor, orgUnits);
    }

    @Override
    public void onBind(@NonNull OrgUnitHolder viewHolder, @NonNull OrgUnitViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}

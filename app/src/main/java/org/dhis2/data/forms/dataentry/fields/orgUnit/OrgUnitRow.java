package org.dhis2.data.forms.dataentry.fields.orgUnit;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormOrgUnitBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */

public class OrgUnitRow implements Row<OrgUnitHolder, OrgUnitViewModel> {

    private final boolean isBgTransparent;
    private final FlowableProcessor<RowAction> processor;
    private final LayoutInflater inflater;
    private final FragmentManager fm;
    private final String renderType;
    private final MutableLiveData<String> currentSelection;
    private boolean isSearchMode = false;

    public OrgUnitRow(FragmentManager fm, LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor,
                      boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.fm = fm;
        this.renderType = null;
        this.isSearchMode = true;
        this.currentSelection = null;
    }

    public OrgUnitRow(FragmentManager fm, LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor,
                      boolean isBgTransparent, String renderType, MutableLiveData<String> currentSelection) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.fm = fm;
        this.renderType = renderType;
        this.currentSelection = currentSelection;
    }

    @NonNull
    @Override
    public OrgUnitHolder onCreate(@NonNull ViewGroup parent) {
        FormOrgUnitBinding binding = DataBindingUtil.inflate(inflater, R.layout.form_org_unit, parent, false);
        binding.orgUnitView.setLayoutData(isBgTransparent, renderType);
        binding.orgUnitView.setFragmentManager(fm);
        return new OrgUnitHolder(binding, processor, isSearchMode, currentSelection);
    }

    @Override
    public void onBind(@NonNull OrgUnitHolder viewHolder, @NonNull OrgUnitViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}

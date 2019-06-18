package org.dhis2.data.forms.dataentry.fields.orgUnit;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormButtonBinding;
import org.dhis2.databinding.FormOrgUnitBinding;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;

import io.reactivex.Observable;
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
    private boolean isSearchMode = false;

    public OrgUnitRow(FragmentManager fm, LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor,
                      boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.fm = fm;
        this.renderType = null;
        this.isSearchMode = true;
    }

    public OrgUnitRow(FragmentManager fm, LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor,
                      boolean isBgTransparent, String renderType) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.fm = fm;
        this.renderType = renderType;
    }

    @NonNull
    @Override
    public OrgUnitHolder onCreate(@NonNull ViewGroup parent) {
        FormOrgUnitBinding binding = DataBindingUtil.inflate(inflater, R.layout.form_org_unit, parent, false);
        binding.orgUnitView.setLayoutData(isBgTransparent, renderType);
        binding.orgUnitView.setFragmentManager(fm);
        return new OrgUnitHolder(binding, processor, isSearchMode);
    }

    @Override
    public void onBind(@NonNull OrgUnitHolder viewHolder, @NonNull OrgUnitViewModel viewModel) {
        viewHolder.update(viewModel);
    }

    @Override
    public void deAttach(@NonNull OrgUnitHolder viewHolder) {
        viewHolder.dispose();
    }
}

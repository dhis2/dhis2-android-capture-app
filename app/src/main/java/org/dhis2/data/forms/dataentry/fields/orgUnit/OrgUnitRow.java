package org.dhis2.data.forms.dataentry.fields.orgUnit;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentManager;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormButtonBinding;
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
    private final Observable<List<OrganisationUnitModel>> orgUnits;
    private final String renderType;
    private FormButtonBinding binding;
    private boolean isSearchMode = false;
    private Observable<List<OrganisationUnitLevel>> levels;

    public OrgUnitRow(FragmentManager fm, LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor,
                      boolean isBgTransparent, Observable<List<OrganisationUnitModel>> orgUnits,
                      Observable<List<OrganisationUnitLevel>> levels) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.fm = fm;
        this.orgUnits = orgUnits;
        this.renderType = null;
        this.isSearchMode = true;
        this.levels = levels;
    }

    public OrgUnitRow(FragmentManager fm, LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor,
                      @NonNull FlowableProcessor<Integer> currentPosition,
                      boolean isBgTransparent, Observable<List<OrganisationUnitModel>> orgUnits, String renderType,
                      Observable<List<OrganisationUnitLevel>> levels) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.fm = fm;
        this.orgUnits = orgUnits;
        this.renderType = renderType;
        this.levels = levels;
    }

    @NonNull
    @Override
    public OrgUnitHolder onCreate(@NonNull ViewGroup parent) {
        ViewDataBinding binding = DataBindingUtil.inflate(
                inflater,
                isBgTransparent ? R.layout.custom_text_view : R.layout.custom_text_view_accent,
                parent,
                false
        );
        binding.setVariable(BR.renderType, renderType);
        binding.executePendingBindings();

        binding.getRoot().findViewById(R.id.input_editText).setFocusable(false); //Makes editText
        binding.getRoot().findViewById(R.id.input_editText).setClickable(true);//  but clickable

        return new OrgUnitHolder(fm, binding, processor, orgUnits, levels);
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

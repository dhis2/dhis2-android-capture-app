package org.dhis2.data.forms.dataentry.tablefields.orgUnit;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentManager;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.Row;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;

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

        return new OrgUnitHolder(fm, binding, processor);
    }

    @Override
    public void onBind(@NonNull OrgUnitHolder viewHolder, @NonNull OrgUnitViewModel viewModel, String value) {
        viewHolder.update(viewModel);
    }

    @Override
    public void deAttach(@NonNull OrgUnitHolder viewHolder) {
        viewHolder.dispose();
    }
}

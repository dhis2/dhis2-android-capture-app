package org.dhis2.data.forms.dataentry.tablefields.edittext;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.Row;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class EditTextRow implements Row<EditTextCustomHolder, EditTextModel> {

    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final boolean isBgTransparent;
    private final String renderType;
    private final ObservableBoolean isEditable;

    public EditTextRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = null;
        this.isEditable = new ObservableBoolean(true);
    }

    public EditTextRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, boolean isBgTransparent, String renderType, ObservableBoolean isEditable) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = renderType;
        this.isEditable = isEditable;
    }

    @NonNull
    @Override
    public EditTextCustomHolder onCreate(@NonNull ViewGroup viewGroup) {
        ViewDataBinding binding = DataBindingUtil.inflate(
                inflater,
                isBgTransparent ? R.layout.custom_text_view : R.layout.custom_text_view_accent,
                viewGroup,
                false
        );
        binding.setVariable(BR.renderType, renderType);

        binding.executePendingBindings();
        return new EditTextCustomHolder(viewGroup, binding
                , processor, isBgTransparent, renderType, isEditable);
    }

    @Override
    public void onBind(@NonNull EditTextCustomHolder viewHolder, @NonNull EditTextModel viewModel) {
        viewHolder.update(viewModel);
    }

    @Override
    public void deAttach(@NonNull EditTextCustomHolder viewHolder) {
        viewHolder.dispose();
    }

}

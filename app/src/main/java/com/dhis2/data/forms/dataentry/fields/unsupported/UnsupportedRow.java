package com.dhis2.data.forms.dataentry.fields.unsupported;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormUnsupportedBinding;

import io.reactivex.processors.FlowableProcessor;

public class UnsupportedRow implements Row<UnsupportedHolder, UnsupportedViewModel> {
    private final String renderType;
    FormUnsupportedBinding binding;
    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final boolean isBgTransparent;

    public UnsupportedRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = null;
    }

    public UnsupportedRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor, boolean isBgTransparent, String renderType) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = renderType;
    }

    @NonNull
    @Override
    public UnsupportedHolder onCreate(@NonNull ViewGroup parent) {
        binding = DataBindingUtil.inflate(inflater, R.layout.form_unsupported, parent, false);
       /* if (isBgTransparent)
            binding.formButton.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.gray_b2b));
        else
            binding.formButton.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.gray_b2b));*/

        return new UnsupportedHolder(binding);
    }

    @Override
    public void onBind(@NonNull UnsupportedHolder viewHolder, @NonNull UnsupportedViewModel viewModel) {
        viewHolder.update(viewModel);
    }

    @Override
    public void deAttach(@NonNull UnsupportedHolder viewHolder) {
        viewHolder.dispose();
    }
}

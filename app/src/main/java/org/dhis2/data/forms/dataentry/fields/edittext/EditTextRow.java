package org.dhis2.data.forms.dataentry.fields.edittext;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormEditTextCustomBinding;

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
    private final boolean isLongText;
    private final MutableLiveData<String> currentSelection;
    private boolean isSearchMode = false;

    //Search form constructor
    public EditTextRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, boolean isBgTransparent, boolean isLongText) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = null;
        this.isSearchMode = true;
        this.isLongText = isLongText;
        this.currentSelection = null;
    }

    //Data entryconstructor
    public EditTextRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor,
                       boolean isBgTransparent, String renderType, boolean isLongText, MutableLiveData<String> currentSelection) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = renderType;
        this.isLongText = isLongText;
        this.currentSelection = currentSelection;
    }

    @NonNull
    @Override
    public EditTextCustomHolder onCreate(@NonNull ViewGroup viewGroup) {
        FormEditTextCustomBinding binding = DataBindingUtil.inflate(inflater, R.layout.form_edit_text_custom, viewGroup, false);
        binding.customEdittext.setLayoutData(isBgTransparent,isLongText);
        binding.customEdittext.setRenderType(renderType);
        return new EditTextCustomHolder(binding, processor, isSearchMode, currentSelection);
    }

    @Override
    public void onBind(@NonNull EditTextCustomHolder viewHolder, @NonNull EditTextModel viewModel) {
        viewHolder.update(viewModel);
    }
}

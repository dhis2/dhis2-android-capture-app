package org.dhis2.data.forms.dataentry.tablefields.edittext;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.Row;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomTextViewCellBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class EditTextRow implements Row<EditTextCellCustomHolder, EditTextModel> {

    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final ObservableBoolean isEditable;

    public EditTextRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, ObservableBoolean isEditable) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isEditable = isEditable;
    }

    @NonNull
    @Override
    public EditTextCellCustomHolder onCreate(@NonNull ViewGroup viewGroup) {
        CustomTextViewCellBinding binding = DataBindingUtil.inflate(
                inflater,
                R.layout.custom_text_view_cell ,
                viewGroup,
                false
        );
        return new EditTextCellCustomHolder(binding, processor, isEditable);
    }

    @Override
    public void onBind(@NonNull EditTextCellCustomHolder viewHolder, @NonNull EditTextModel viewModel, String value) {
        viewHolder.update(viewModel, value);
    }

    @Override
    public void deAttach(@NonNull EditTextCellCustomHolder viewHolder) {
        viewHolder.dispose();
    }

}

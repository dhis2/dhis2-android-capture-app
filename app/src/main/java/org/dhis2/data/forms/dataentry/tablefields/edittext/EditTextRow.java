package org.dhis2.data.forms.dataentry.tablefields.edittext;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.evrencoskun.tableview.TableView;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.Row;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomTextViewCellBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetTableAdapter;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
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
    private final TableView tableView;
    private final ObservableField<DataSetTableAdapter.TableScale> tableScale;

    public EditTextRow(@NonNull LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor,
                       ObservableBoolean isEditable, TableView tableView, ObservableField<DataSetTableAdapter.TableScale> tableScale) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isEditable = isEditable;
        this.tableView = tableView;
        this.tableScale = tableScale;
    }

    @NonNull
    @Override
    public EditTextCellCustomHolder onCreate(@NonNull ViewGroup viewGroup) {
        CustomTextViewCellBinding binding = DataBindingUtil.inflate(
                inflater,
                R.layout.custom_text_view_cell,
                viewGroup,
                false
        );
        binding.setTableScale(tableScale);
        return new EditTextCellCustomHolder(binding, processor, isEditable, tableView);
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

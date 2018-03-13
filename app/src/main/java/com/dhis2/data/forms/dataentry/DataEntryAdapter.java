package com.dhis2.data.forms.dataentry;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeRow;
import com.dhis2.data.forms.dataentry.fields.edittext.EditTextRow;
import com.dhis2.data.forms.dataentry.fields.spinner.SpinnerRow;
import com.dhis2.databinding.FormEditTextCustomBinding;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

final class DataEntryAdapter extends Adapter {
    private final int EDITTEXT = 0;
    private final int BUTTON = 1;
    private final int CHECKBOX = 2;
    private final int SPINNER = 3;
    private final int COORDINATES = 4;
    private final int TIME = 5;
    private final int DATE = 6;
    private final int DATETIME = 7;
    private final int AGEVIEW = 8;
    private final int YES_NO = 9;

    @NonNull
    private final List<FieldViewModel> viewModels;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    @NonNull
    private final List<Row> rows;

    DataEntryAdapter(@NonNull LayoutInflater layoutInflater,
                     @NonNull FragmentManager fragmentManager,
                     @NonNull DataEntryArguments dataEntryArguments) {
        rows = new ArrayList<>();
        viewModels = new ArrayList<>();
        processor = PublishProcessor.create();
//TODO: CHECK ROWS
        rows.add(EDITTEXT, new EditTextRow(processor));
        rows.add(BUTTON, new EditTextRow(processor));
        rows.add(CHECKBOX, new EditTextRow(processor));
        rows.add(SPINNER, new SpinnerRow(processor));
        rows.add(COORDINATES, new EditTextRow(processor));
        rows.add(TIME, new DateTimeRow(processor));
        rows.add(DATE, new DateTimeRow(processor));
        rows.add(DATETIME, new DateTimeRow(processor));
        rows.add(AGEVIEW, new EditTextRow(processor));
        rows.add(YES_NO, new EditTextRow(processor));
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FormEditTextCustomBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.form_edit_text_custom, parent, false);
        return rows.get(viewType).onCreate(binding, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        rows.get(holder.getItemViewType()).onBind(holder,
                viewModels.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    @Override
    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
        //TODO: CHECK VIEWMODELS
        FieldViewModel viewModel = viewModels.get(position);
        return EDITTEXT;
       /* if (viewModel instanceof EditTextModel) {
            return EDITTEXT;
        } else
            return super.getItemViewType(position);*/
       /* if (viewModel instanceof CheckBoxViewModel) {
            return ROW_CHECKBOX;
        } else if (viewModel instanceof EditTextModel) {
            return ROW_EDITTEXT;
        } else if (viewModel instanceof RadioButtonViewModel) {
            return ROW_RADIO_BUTTONS;
        } else if (viewModel instanceof TextViewModel) {
            return ROW_TEXT;
        } else if (viewModel instanceof OptionsViewModel) {
            return ROW_OPTIONS;
        } else if (viewModel instanceof DateViewModel) {
            return ROW_DATE;
        } else {
            throw new IllegalStateException("Unsupported view model type: "
                    + viewModel.getClass());
        }*/
    }

    @Override
    public long getItemId(int position) {
        return viewModels.get(position).uid().hashCode();
    }

    @NonNull
    FlowableProcessor<RowAction> asFlowable() {
        return processor;
    }

    void swap(@NonNull List<FieldViewModel> updates) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new DataEntryDiffCallback(viewModels, updates));

        viewModels.clear();
        viewModels.addAll(updates);

        diffResult.dispatchUpdatesTo(this);
    }
}

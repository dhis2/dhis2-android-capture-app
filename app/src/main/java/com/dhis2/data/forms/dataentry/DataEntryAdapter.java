package com.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.data.forms.dataentry.fields.Row;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.data.forms.dataentry.fields.coordinate.CoordinateRow;
import com.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeRow;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel;
import com.dhis2.data.forms.dataentry.fields.edittext.EditTextModel;
import com.dhis2.data.forms.dataentry.fields.edittext.EditTextRow;
import com.dhis2.data.forms.dataentry.fields.file.FileRow;
import com.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonRow;
import com.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel;
import com.dhis2.data.forms.dataentry.fields.spinner.SpinnerRow;
import com.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;

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
        rows.add(EDITTEXT, new EditTextRow(layoutInflater, processor));
        rows.add(BUTTON, new FileRow());
        rows.add(CHECKBOX, new RadioButtonRow(layoutInflater, processor));
        rows.add(SPINNER, new SpinnerRow(layoutInflater, processor));
        rows.add(COORDINATES, new CoordinateRow(layoutInflater, processor));
        rows.add(TIME, new DateTimeRow(layoutInflater, processor));
        rows.add(DATE, new DateTimeRow(layoutInflater, processor));
        rows.add(DATETIME, new DateTimeRow(layoutInflater, processor));
        rows.add(AGEVIEW, new EditTextRow(layoutInflater, processor));
        rows.add(YES_NO, new RadioButtonRow(layoutInflater, processor));
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return rows.get(viewType).onCreate(parent);
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
        if (viewModel instanceof EditTextModel) {
            return EDITTEXT;
       /* } else if (viewModel instanceof CheckBoxViewModel) {
            return BUTTON;*/
        } else if (viewModel instanceof RadioButtonViewModel) {
            return CHECKBOX;
        } else if (viewModel instanceof SpinnerViewModel) {
            return SPINNER;
        } else if (viewModel instanceof CoordinateViewModel) {
            return COORDINATES;
       /* } else if (viewModel instanceof OptionsViewModel) {
            return TIME;
        } else if (viewModel instanceof DateViewModel) {
            return DATE;*/
        } else if (viewModel instanceof DateTimeViewModel) {
            return DATETIME;
      /*  } else if (viewModel instanceof DateViewModel) {
            return AGEVIEW;
        } else if (viewModel instanceof YesNoView) {
            return YES_NO;*/
        } else {
            throw new IllegalStateException("Unsupported view model type: "
                    + viewModel.getClass());
        }
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

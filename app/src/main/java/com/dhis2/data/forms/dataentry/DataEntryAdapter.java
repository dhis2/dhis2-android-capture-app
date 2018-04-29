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
import com.dhis2.data.forms.dataentry.fields.age.AgeRow;
import com.dhis2.data.forms.dataentry.fields.age.AgeViewModel;
import com.dhis2.data.forms.dataentry.fields.coordinate.CoordinateRow;
import com.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeRow;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel;
import com.dhis2.data.forms.dataentry.fields.edittext.EditTextModel;
import com.dhis2.data.forms.dataentry.fields.edittext.EditTextRow;
import com.dhis2.data.forms.dataentry.fields.file.FileRow;
import com.dhis2.data.forms.dataentry.fields.file.FileViewModel;
import com.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitRow;
import com.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import com.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonRow;
import com.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel;
import com.dhis2.data.forms.dataentry.fields.spinner.SpinnerRow;
import com.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;

import org.hisp.dhis.android.core.common.ValueType;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class DataEntryAdapter extends Adapter {
    private static final int EDITTEXT = 0;
    private static final int BUTTON = 1;
    private static final int CHECKBOX = 2;
    private static final int SPINNER = 3;
    private static final int COORDINATES = 4;
    private static final int TIME = 5;
    private static final int DATE = 6;
    private static final int DATETIME = 7;
    private static final int AGEVIEW = 8;
    private static final int YES_NO = 9;
    private static final int ORG_UNIT = 10;

    @NonNull
    private final List<FieldViewModel> viewModels;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    @NonNull
    private final List<Row> rows;

    public DataEntryAdapter(@NonNull LayoutInflater layoutInflater,
                     @NonNull FragmentManager fragmentManager,
                     @NonNull DataEntryArguments dataEntryArguments) {
        setHasStableIds(true);
        rows = new ArrayList<>();
        viewModels = new ArrayList<>();
        processor = PublishProcessor.create();

        rows.add(EDITTEXT, new EditTextRow(layoutInflater, processor, true));
        rows.add(BUTTON, new FileRow(layoutInflater, processor, true));
        rows.add(CHECKBOX, new RadioButtonRow(layoutInflater, processor, true));
        rows.add(SPINNER, new SpinnerRow(layoutInflater, processor, true));
        rows.add(COORDINATES, new CoordinateRow(layoutInflater, processor, true));
        rows.add(TIME, new DateTimeRow(layoutInflater, processor, TIME, true));
        rows.add(DATE, new DateTimeRow(layoutInflater, processor, DATE, true));
        rows.add(DATETIME, new DateTimeRow(layoutInflater, processor, DATETIME, true));
        rows.add(AGEVIEW, new AgeRow(layoutInflater, processor, true));
        rows.add(YES_NO, new RadioButtonRow(layoutInflater, processor, true));
        rows.add(ORG_UNIT, new OrgUnitRow(layoutInflater, processor, false));

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

        FieldViewModel viewModel = viewModels.get(position);
        if (viewModel instanceof EditTextModel) {
            return EDITTEXT;
        } else if (viewModel instanceof RadioButtonViewModel) {
            return CHECKBOX;
        } else if (viewModel instanceof SpinnerViewModel) {
            return SPINNER;
        } else if (viewModel instanceof CoordinateViewModel) {
            return COORDINATES;

        } else if (viewModel instanceof DateTimeViewModel) {
            if (((DateTimeViewModel) viewModel).valueType() == ValueType.DATE)
                return DATE;
            if (((DateTimeViewModel) viewModel).valueType() == ValueType.TIME)
                return TIME;
            else
                return DATETIME;
        } else if (viewModel instanceof AgeViewModel) {
            return AGEVIEW;
        } else if (viewModel instanceof FileViewModel) {
            return BUTTON;
        } else if (viewModel instanceof OrgUnitViewModel) {
            return ORG_UNIT;
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

    public void swap(@NonNull List<FieldViewModel> updates) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new DataEntryDiffCallback(viewModels, updates));

        viewModels.clear();
        viewModels.addAll(updates);

        diffResult.dispatchUpdatesTo(this);
    }
}

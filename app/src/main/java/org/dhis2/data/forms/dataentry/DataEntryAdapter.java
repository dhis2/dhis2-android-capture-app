package org.dhis2.data.forms.dataentry;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.forms.dataentry.fields.age.AgeRow;
import org.dhis2.data.forms.dataentry.fields.age.AgeViewModel;
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateRow;
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel;
import org.dhis2.data.forms.dataentry.fields.datetime.DateTimeRow;
import org.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel;
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextModel;
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextRow;
import org.dhis2.data.forms.dataentry.fields.file.FileRow;
import org.dhis2.data.forms.dataentry.fields.file.FileViewModel;
import org.dhis2.data.forms.dataentry.fields.image.ImageRow;
import org.dhis2.data.forms.dataentry.fields.image.ImageViewModel;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitRow;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonRow;
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerRow;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedRow;
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
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
    private static final int IMAGE = 11;
    private static final int UNSUPPORTED = 12;


    @NonNull
    private final List<FieldViewModel> viewModels;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    @NonNull
    private final FlowableProcessor<Integer> currentPosition;

    @NonNull
    private final ObservableField<String> imageSelector;

    @NonNull
    private final List<Row> rows;
    private final DataEntryArguments dataEntryArguments;

    private final FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;

    public DataEntryAdapter(@NonNull LayoutInflater layoutInflater,
                            @NonNull FragmentManager fragmentManager,
                            @NonNull DataEntryArguments dataEntryArguments,
                            @NonNull Observable<List<OrganisationUnitModel>> orgUnits,
                            ObservableBoolean isEditable) { //TODO: Add isEditable to all fields and test if can be changed on the fly
        setHasStableIds(true);
        rows = new ArrayList<>();
        viewModels = new ArrayList<>();
        processor = PublishProcessor.create();
        imageSelector = new ObservableField<>("");
        currentPosition = PublishProcessor.create();
        this.processorOptionSet = PublishProcessor.create();
        this.dataEntryArguments = dataEntryArguments;

        rows.add(EDITTEXT, new EditTextRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType(), isEditable));
        rows.add(BUTTON, new FileRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(CHECKBOX, new RadioButtonRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(SPINNER, new SpinnerRow(layoutInflater, processor, currentPosition, processorOptionSet, true, dataEntryArguments.renderType()));
        rows.add(COORDINATES, new CoordinateRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(TIME, new DateTimeRow(layoutInflater, processor, currentPosition, TIME, true, dataEntryArguments.renderType()));
        rows.add(DATE, new DateTimeRow(layoutInflater, processor, currentPosition, DATE, true, dataEntryArguments.renderType()));
        rows.add(DATETIME, new DateTimeRow(layoutInflater, processor, currentPosition, DATETIME, true, dataEntryArguments.renderType()));
        rows.add(AGEVIEW, new AgeRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(YES_NO, new RadioButtonRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(ORG_UNIT, new OrgUnitRow(fragmentManager, layoutInflater, processor, currentPosition, true, orgUnits, dataEntryArguments.renderType()));
        rows.add(IMAGE, new ImageRow(layoutInflater, processor, currentPosition, dataEntryArguments.renderType()));
        rows.add(UNSUPPORTED, new UnsupportedRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));

    }

    public DataEntryAdapter(@NonNull LayoutInflater layoutInflater,
                            @NonNull FragmentManager fragmentManager,
                            @NonNull DataEntryArguments dataEntryArguments,
                            @NonNull Observable<List<OrganisationUnitModel>> orgUnits,
                            ObservableBoolean isEditable,
                            @NonNull FlowableProcessor<RowAction> processor) { //TODO: Add isEditable to all fields and test if can be changed on the fly
        setHasStableIds(true);
        rows = new ArrayList<>();
        viewModels = new ArrayList<>();
        this.processor = processor;
        imageSelector = new ObservableField<>("");
        currentPosition = PublishProcessor.create();
        this.processorOptionSet = PublishProcessor.create();
        this.dataEntryArguments = dataEntryArguments;

        rows.add(EDITTEXT, new EditTextRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType(), isEditable));
        rows.add(BUTTON, new FileRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(CHECKBOX, new RadioButtonRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(SPINNER, new SpinnerRow(layoutInflater, processor, currentPosition, processorOptionSet, true, dataEntryArguments.renderType()));
        rows.add(COORDINATES, new CoordinateRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(TIME, new DateTimeRow(layoutInflater, processor, currentPosition, TIME, true, dataEntryArguments.renderType()));
        rows.add(DATE, new DateTimeRow(layoutInflater, processor, currentPosition, DATE, true, dataEntryArguments.renderType()));
        rows.add(DATETIME, new DateTimeRow(layoutInflater, processor, currentPosition, DATETIME, true, dataEntryArguments.renderType()));
        rows.add(AGEVIEW, new AgeRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(YES_NO, new RadioButtonRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(ORG_UNIT, new OrgUnitRow(fragmentManager, layoutInflater, processor, currentPosition, true, orgUnits, dataEntryArguments.renderType()));
        rows.add(IMAGE, new ImageRow(layoutInflater, processor, currentPosition, dataEntryArguments.renderType()));
        rows.add(UNSUPPORTED, new UnsupportedRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == IMAGE)
            return ((ImageRow) rows.get(IMAGE)).onCreate(parent, getItemCount(), imageSelector);
        else
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
        } else if (viewModel instanceof ImageViewModel) {
            return IMAGE;
        } else if (viewModel instanceof UnsupportedViewModel) {
            return UNSUPPORTED;
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
    public FlowableProcessor<RowAction> asFlowable() {
        return processor;
    }

    public FlowableProcessor<Trio<String, String, Integer>> asFlowableOption(){
        return processorOptionSet;
    }

    public void swap(@NonNull List<FieldViewModel> updates) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new DataEntryDiffCallback(viewModels, updates));

        viewModels.clear();
        viewModels.addAll(updates);

        diffResult.dispatchUpdatesTo(this);
    }

    public boolean mandatoryOk() {
        boolean isOk = true;
        for (FieldViewModel fieldViewModel : viewModels) {
            if (fieldViewModel.mandatory() && (fieldViewModel.value() == null || fieldViewModel.value().isEmpty()))
                isOk = false;
        }

        return isOk;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        rows.get(holder.getItemViewType()).deAttach(holder);
    }

    public boolean hasError() {
        boolean hasError = false;
        for (FieldViewModel fieldViewModel : viewModels) {
            if (fieldViewModel.error() != null)
                hasError = true;
        }

        return hasError;
    }
}

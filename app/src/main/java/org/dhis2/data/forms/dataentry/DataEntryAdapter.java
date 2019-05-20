package org.dhis2.data.forms.dataentry;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.forms.dataentry.fields.age.AgeRow;
import org.dhis2.data.forms.dataentry.fields.age.AgeViewModel;
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateRow;
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel;
import org.dhis2.data.forms.dataentry.fields.datetime.DateTimeRow;
import org.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel;
import org.dhis2.data.forms.dataentry.fields.display.DisplayRow;
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel;
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
import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
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
    private static final int LONG_TEXT = 13;
    private static final int DISPLAY = 14;


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

    private final Observable<List<OrganisationUnitLevel>> levels;

    private final RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            focusPosition = positionStart;
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            focusPosition = positionStart;
        }
    };
    private int focusPosition = -1;

    public DataEntryAdapter(@NonNull LayoutInflater layoutInflater,
                            @NonNull FragmentManager fragmentManager,
                            @NonNull DataEntryArguments dataEntryArguments,
                            @NonNull Observable<List<OrganisationUnitModel>> orgUnits,
                            ObservableBoolean isEditable,
                            Observable<List<OrganisationUnitLevel>> levels) { //TODO: Add isEditable to all fields and test if can be changed on the fly
        setHasStableIds(true);
        rows = new ArrayList<>();
        viewModels = new ArrayList<>();
        processor = PublishProcessor.create();
        imageSelector = new ObservableField<>("");
        currentPosition = PublishProcessor.create();
        this.processorOptionSet = PublishProcessor.create();
        this.dataEntryArguments = dataEntryArguments;
        this.levels = levels;

        rows.add(EDITTEXT, new EditTextRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType(), isEditable, false));
        rows.add(BUTTON, new FileRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(CHECKBOX, new RadioButtonRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(SPINNER, new SpinnerRow(layoutInflater, processor, currentPosition, processorOptionSet, true, dataEntryArguments.renderType()));
        rows.add(COORDINATES, new CoordinateRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(TIME, new DateTimeRow(layoutInflater, processor, currentPosition, TIME, true, dataEntryArguments.renderType()));
        rows.add(DATE, new DateTimeRow(layoutInflater, processor, currentPosition, DATE, true, dataEntryArguments.renderType()));
        rows.add(DATETIME, new DateTimeRow(layoutInflater, processor, currentPosition, DATETIME, true, dataEntryArguments.renderType()));
        rows.add(AGEVIEW, new AgeRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(YES_NO, new RadioButtonRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(ORG_UNIT, new OrgUnitRow(fragmentManager, layoutInflater, processor, currentPosition, true, orgUnits, dataEntryArguments.renderType(), levels));
        rows.add(IMAGE, new ImageRow(layoutInflater, processor, currentPosition, dataEntryArguments.renderType()));
        rows.add(UNSUPPORTED, new UnsupportedRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(LONG_TEXT, new EditTextRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType(), isEditable, true));
        rows.add(DISPLAY, new DisplayRow(layoutInflater));

        registerAdapterDataObserver(adapterDataObserver);

    }

    public DataEntryAdapter(@NonNull LayoutInflater layoutInflater,
                            @NonNull FragmentManager fragmentManager,
                            @NonNull DataEntryArguments dataEntryArguments,
                            @NonNull Observable<List<OrganisationUnitModel>> orgUnits,
                            ObservableBoolean isEditable,//TODO: Add isEditable to all fields and test if can be changed on the fly
                            @NonNull FlowableProcessor<RowAction> processor,
                            @NonNull FlowableProcessor<Trio<String, String, Integer>> processorOptSet,
                            Observable<List<OrganisationUnitLevel>> levels) {
        setHasStableIds(true);
        rows = new ArrayList<>();
        viewModels = new ArrayList<>();
        this.processor = processor;
        imageSelector = new ObservableField<>("");
        currentPosition = PublishProcessor.create();
        this.processorOptionSet = processorOptSet;
        this.dataEntryArguments = dataEntryArguments;
        this.levels = levels;

        rows.add(EDITTEXT, new EditTextRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType(), isEditable, false));
        rows.add(BUTTON, new FileRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(CHECKBOX, new RadioButtonRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(SPINNER, new SpinnerRow(layoutInflater, processor, currentPosition, processorOptionSet, true, dataEntryArguments.renderType()));
        rows.add(COORDINATES, new CoordinateRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(TIME, new DateTimeRow(layoutInflater, processor, currentPosition, TIME, true, dataEntryArguments.renderType()));
        rows.add(DATE, new DateTimeRow(layoutInflater, processor, currentPosition, DATE, true, dataEntryArguments.renderType()));
        rows.add(DATETIME, new DateTimeRow(layoutInflater, processor, currentPosition, DATETIME, true, dataEntryArguments.renderType()));
        rows.add(AGEVIEW, new AgeRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(YES_NO, new RadioButtonRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(ORG_UNIT, new OrgUnitRow(fragmentManager, layoutInflater, processor, currentPosition, true, orgUnits, dataEntryArguments.renderType(), levels));
        rows.add(IMAGE, new ImageRow(layoutInflater, processor, currentPosition, dataEntryArguments.renderType()));
        rows.add(UNSUPPORTED, new UnsupportedRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType()));
        rows.add(LONG_TEXT, new EditTextRow(layoutInflater, processor, currentPosition, true, dataEntryArguments.renderType(), isEditable, true));
        rows.add(DISPLAY, new DisplayRow(layoutInflater));

        registerAdapterDataObserver(adapterDataObserver);

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

        if(position == focusPosition)
            holder.itemView.requestFocus();
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    @Override
    public int getItemViewType(int position) {

        FieldViewModel viewModel = viewModels.get(position);
        if (viewModel instanceof EditTextModel) {
            if (((EditTextModel) viewModel).valueType() != ValueType.LONG_TEXT)
                return EDITTEXT;
            else
                return LONG_TEXT;
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
        } else if (viewModel instanceof DisplayViewModel) {
            return DISPLAY;
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

    public FlowableProcessor<Trio<String, String, Integer>> asFlowableOption() {
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

    public void notifyChanges(RowAction rowAction) {
        List<FieldViewModel> helperModels = new ArrayList<>();
        for (FieldViewModel field : viewModels) {
            FieldViewModel toAdd = field;
            if (field.uid().equals(rowAction.id()))
                toAdd = field.withValue(rowAction.optionName() == null ? rowAction.value() : rowAction.optionName()).withEditMode(toAdd.editable());

            helperModels.add(toAdd);
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new DataEntryDiffCallback(viewModels, helperModels));

        viewModels.clear();
        viewModels.addAll(helperModels);

        diffResult.dispatchUpdatesTo(this);
    }

}

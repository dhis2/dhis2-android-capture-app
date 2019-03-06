package org.dhis2.data.forms.dataentry;

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
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import io.reactivex.Observable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class DataEntryAdapter extends Adapter {

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

        initRows(layoutInflater, fragmentManager, dataEntryArguments, orgUnits, isEditable);
    }

    private void initRows(@NonNull LayoutInflater layoutInflater,
                          @NonNull FragmentManager fragmentManager,
                          @NonNull DataEntryArguments dataEntryArguments,
                          @NonNull Observable<List<OrganisationUnitModel>> orgUnits,
                          ObservableBoolean isEditable) {
        rows.add(Constants.EDITTEXT, new EditTextRow(layoutInflater, processor, true, dataEntryArguments.renderType(), isEditable));
        rows.add(Constants.BUTTON, new FileRow(layoutInflater, true));
        rows.add(Constants.CHECKBOX, new RadioButtonRow(layoutInflater, processor, true));
        rows.add(Constants.SPINNER, new SpinnerRow(layoutInflater, processor, processorOptionSet, true, dataEntryArguments.renderType()));
        rows.add(Constants.COORDINATES, new CoordinateRow(layoutInflater, processor, true));
        rows.add(Constants.TIME, new DateTimeRow(layoutInflater, processor, currentPosition, Constants.TIME, true));
        rows.add(Constants.DATE, new DateTimeRow(layoutInflater, processor, currentPosition, Constants.DATE, true));
        rows.add(Constants.DATETIME, new DateTimeRow(layoutInflater, processor, currentPosition, Constants.DATETIME, true));
        rows.add(Constants.AGEVIEW, new AgeRow(layoutInflater, processor, true));
        rows.add(Constants.YES_NO, new RadioButtonRow(layoutInflater, processor, true));
        rows.add(Constants.ORG_UNIT, new OrgUnitRow(fragmentManager, layoutInflater, processor, true, orgUnits, dataEntryArguments.renderType()));
        rows.add(Constants.IMAGE, new ImageRow(layoutInflater, processor, dataEntryArguments.renderType()));
        rows.add(Constants.UNSUPPORTED, new UnsupportedRow(layoutInflater));
    }

    public DataEntryAdapter(@NonNull LayoutInflater layoutInflater,
                            @NonNull FragmentManager fragmentManager,
                            @NonNull DataEntryArguments dataEntryArguments,
                            @NonNull Observable<List<OrganisationUnitModel>> orgUnits,
                            ObservableBoolean isEditable,
                            @NonNull FlowableProcessor<RowAction> processor,
                            @NonNull FlowableProcessor<Trio<String, String, Integer>> processorOptSet) { //TODO: Add isEditable to all fields and test if can be changed on the fly
        setHasStableIds(true);
        rows = new ArrayList<>();
        viewModels = new ArrayList<>();
        this.processor = processor;
        imageSelector = new ObservableField<>("");
        currentPosition = PublishProcessor.create();
        this.processorOptionSet = processorOptSet;

        initRows(layoutInflater, fragmentManager, dataEntryArguments, orgUnits, isEditable);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Constants.IMAGE)
            return ((ImageRow) rows.get(Constants.IMAGE)).onCreate(parent, getItemCount(), imageSelector);
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
            return Constants.EDITTEXT;
        } else if (viewModel instanceof RadioButtonViewModel) {
            return Constants.CHECKBOX;
        } else if (viewModel instanceof SpinnerViewModel) {
            return Constants.SPINNER;
        } else if (viewModel instanceof CoordinateViewModel) {
            return Constants.COORDINATES;
        } else if (viewModel instanceof DateTimeViewModel) {
            return getDateTimeItemViewType(viewModel);
        } else if (viewModel instanceof AgeViewModel) {
            return Constants.AGEVIEW;
        } else if (viewModel instanceof FileViewModel) {
            return Constants.BUTTON;
        } else if (viewModel instanceof OrgUnitViewModel) {
            return Constants.ORG_UNIT;
        } else if (viewModel instanceof ImageViewModel) {
            return Constants.IMAGE;
        } else if (viewModel instanceof UnsupportedViewModel) {
            return Constants.UNSUPPORTED;
        } else {
            throw new IllegalStateException("Unsupported view model type: "
                    + viewModel.getClass());
        }
    }

    private int getDateTimeItemViewType(FieldViewModel viewModel) {
        if (((DateTimeViewModel) viewModel).valueType() == ValueType.DATE)
            return Constants.DATE;
        if (((DateTimeViewModel) viewModel).valueType() == ValueType.TIME)
            return Constants.TIME;
        else
            return Constants.DATETIME;
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
}
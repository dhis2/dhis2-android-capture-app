package org.dhis2.data.forms.dataentry;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.mapbox.geojson.Feature;

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
import org.dhis2.data.forms.dataentry.fields.picture.PictureRow;
import org.dhis2.data.forms.dataentry.fields.picture.PictureViewModel;
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonRow;
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerRow;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedRow;
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedViewModel;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.custom_views.PictureView;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.ValueType;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

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
    private static final int PICTURE = 15;


    @NonNull
    private final List<FieldViewModel> viewModels;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    @NonNull
    private final ObservableField<String> imageSelector;

    @NonNull
    private final List<Row> rows;

    private final FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;

    private MutableLiveData<String> currentFocusUid;

    private String lastFocusItem;
    private int nextFocusPosition = -1;

    public DataEntryAdapter(@NonNull LayoutInflater layoutInflater,
                            @NonNull FragmentManager fragmentManager,
                            @NonNull DataEntryArguments dataEntryArguments) {
        setHasStableIds(true);
        rows = new ArrayList<>();
        viewModels = new ArrayList<>();
        processor = PublishProcessor.create();
        imageSelector = new ObservableField<>("");
        this.processorOptionSet = PublishProcessor.create();
        this.currentFocusUid = new MutableLiveData<>();

        currentFocusUid.observeForever(newValue -> {
            Timber.tag("UID").d("NEW UID %s", newValue);
        });

        rows.add(EDITTEXT, new EditTextRow(layoutInflater, processor, true, dataEntryArguments.renderType(), false, currentFocusUid));
        rows.add(BUTTON, new FileRow(layoutInflater, processor, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(CHECKBOX, new RadioButtonRow(layoutInflater, processor, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(SPINNER, new SpinnerRow(layoutInflater, processor, processorOptionSet, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(COORDINATES, new CoordinateRow(layoutInflater, processor, true, dataEntryArguments.renderType(), currentFocusUid, FeatureType.POINT));
        rows.add(TIME, new DateTimeRow(layoutInflater, processor, TIME, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(DATE, new DateTimeRow(layoutInflater, processor, DATE, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(DATETIME, new DateTimeRow(layoutInflater, processor, DATETIME, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(AGEVIEW, new AgeRow(layoutInflater, processor, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(YES_NO, new RadioButtonRow(layoutInflater, processor, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(ORG_UNIT, new OrgUnitRow(fragmentManager, layoutInflater, processor, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(IMAGE, new ImageRow(layoutInflater, processor, dataEntryArguments.renderType()));
        rows.add(UNSUPPORTED, new UnsupportedRow(layoutInflater));
        rows.add(LONG_TEXT, new EditTextRow(layoutInflater, processor, true, dataEntryArguments.renderType(), true, currentFocusUid));
        rows.add(DISPLAY, new DisplayRow(layoutInflater));
        rows.add(PICTURE, new PictureRow(layoutInflater, processor, true));
    }

    public DataEntryAdapter(@NonNull LayoutInflater layoutInflater,
                            @NonNull FragmentManager fragmentManager,
                            @NonNull DataEntryArguments dataEntryArguments,
                            @NonNull FlowableProcessor<RowAction> processor,
                            @NonNull FlowableProcessor<Trio<String, String, Integer>> processorOptSet) {
        setHasStableIds(true);
        rows = new ArrayList<>();
        viewModels = new ArrayList<>();
        this.processor = processor;
        imageSelector = new ObservableField<>("");
        this.processorOptionSet = processorOptSet;
        this.currentFocusUid = new MutableLiveData<>();

        rows.add(EDITTEXT, new EditTextRow(layoutInflater, processor, true, dataEntryArguments.renderType(), false, currentFocusUid));
        rows.add(BUTTON, new FileRow(layoutInflater, processor, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(CHECKBOX, new RadioButtonRow(layoutInflater, processor, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(SPINNER, new SpinnerRow(layoutInflater, processor, processorOptionSet, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(COORDINATES, new CoordinateRow(layoutInflater, processor, true, dataEntryArguments.renderType(), currentFocusUid, FeatureType.POINT));
        rows.add(TIME, new DateTimeRow(layoutInflater, processor, TIME, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(DATE, new DateTimeRow(layoutInflater, processor, DATE, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(DATETIME, new DateTimeRow(layoutInflater, processor, DATETIME, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(AGEVIEW, new AgeRow(layoutInflater, processor, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(YES_NO, new RadioButtonRow(layoutInflater, processor, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(ORG_UNIT, new OrgUnitRow(fragmentManager, layoutInflater, processor, true, dataEntryArguments.renderType(), currentFocusUid));
        rows.add(IMAGE, new ImageRow(layoutInflater, processor, dataEntryArguments.renderType()));
        rows.add(UNSUPPORTED, new UnsupportedRow(layoutInflater));
        rows.add(LONG_TEXT, new EditTextRow(layoutInflater, processor, true, dataEntryArguments.renderType(), true, currentFocusUid));
        rows.add(DISPLAY, new DisplayRow(layoutInflater));
        rows.add(PICTURE, new PictureRow(layoutInflater, processor, true));
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

     /*   if (position != 0 && position == nextFocusPosition && lastFocusPosition != nextFocusPosition && holder instanceof FormViewHolder) {
            lastFocusPosition = position;
            ((FormViewHolder) holder).performAction();
            if (!(holder instanceof ImageHolder))
                holder.itemView.setBackground(AppCompatResources.getDrawable(holder.itemView.getContext(), R.drawable.item_selected_bg));
        } else if (!(holder instanceof ImageHolder)) {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }*/

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
        }else if (viewModel instanceof PictureViewModel) {
            return PICTURE;
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

    public void swap(@NonNull List<FieldViewModel> updates) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new DataEntryDiffCallback(viewModels, updates));

        viewModels.clear();
        viewModels.addAll(updates);

        int currentFocusPosition = -1;
        int lastFocusPosition = -1;

        if (lastFocusItem != null) {
            nextFocusPosition = -1;
            for (int i = 0; i < updates.size(); i++) {
                if (updates.get(i).uid().equals(lastFocusItem)) {
                    lastFocusPosition = i;
                    nextFocusPosition = i + 1;
                }
                if (i == nextFocusPosition && !updates.get(i).editable()) {
                    nextFocusPosition++;
                }
                if (updates.get(i).uid().equals(currentFocusUid.getValue()))
                    currentFocusPosition = i;
            }
        }

        diffResult.dispatchUpdatesTo(this);

        if (nextFocusPosition != -1 && currentFocusPosition == lastFocusPosition && nextFocusPosition < viewModels.size())
            currentFocusUid.setValue(viewModels.get(nextFocusPosition).uid());
        else if (currentFocusPosition != -1 && currentFocusPosition < viewModels.size())
            currentFocusUid.setValue(viewModels.get(currentFocusPosition).uid());
    }

    public void swapWithoutList() {

        if (lastFocusItem != null) {
            nextFocusPosition = -1;
            for (int i = 0; i < viewModels.size(); i++) {
                if (viewModels.get(i).uid().equals(lastFocusItem))
                    nextFocusPosition = i + 1;
                if (i == nextFocusPosition && !viewModels.get(i).editable()) {
                    nextFocusPosition++;
                }
            }
        }

        if (nextFocusPosition != -1)
            currentFocusUid.setValue(viewModels.get(nextFocusPosition).uid());
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

    public String getErrorFieldNames() {
        StringBuilder errorFieldNames = new StringBuilder();
        for (FieldViewModel fieldViewModel : viewModels) {
            if (fieldViewModel.error() != null) {
                if (errorFieldNames.length() == 0)
                    errorFieldNames.append(fieldViewModel.label());
                else
                    errorFieldNames.append(", ").append(fieldViewModel.label());
            }
        }
        return errorFieldNames.toString();
    }

    public void setLastFocusItem(String lastFocusItem) {
        currentFocusUid.setValue(lastFocusItem);
        this.nextFocusPosition = -1;
        this.lastFocusItem = lastFocusItem;
    }

}

package org.dhis2.data.forms.dataentry;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.dhis2.R;
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
import org.dhis2.data.forms.dataentry.fields.image.ImageHolder;
import org.dhis2.data.forms.dataentry.fields.image.ImageRow;
import org.dhis2.data.forms.dataentry.fields.image.ImageViewModel;
import org.dhis2.data.forms.dataentry.fields.optionset.OptionSetRow;
import org.dhis2.data.forms.dataentry.fields.optionset.OptionSetViewModel;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitRow;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import org.dhis2.data.forms.dataentry.fields.picture.PictureRow;
import org.dhis2.data.forms.dataentry.fields.picture.PictureViewModel;
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonRow;
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel;
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextRow;
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextViewModel;
import org.dhis2.data.forms.dataentry.fields.section.SectionHolder;
import org.dhis2.data.forms.dataentry.fields.section.SectionRow;
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerRow;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedRow;
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedViewModel;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FormSectionBinding;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class DataEntryAdapter extends ListAdapter<FieldViewModel, ViewHolder> {

    private static final int SECTION = 17;
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
    private static final int SCAN_CODE = 16;
    private static final int OPTION_SET_SELECT = 18;


    @NonNull
    private final List<FieldViewModel> viewModels;

    @NonNull
    private final FlowableProcessor<RowAction> processor;

    @NonNull
    private final ObservableField<String> imageSelector;

    @NonNull
    private final List<Row> rows;

    private final FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;
    private final ObservableField<String> selectedSection;
    private final FlowableProcessor<String> sectionProcessor;

    private MutableLiveData<String> currentFocusUid;

    private String lastFocusItem;
    private int nextFocusPosition = -1;

    List<Integer> sectionPositions;
    private String rendering = ProgramStageSectionRenderingType.LISTING.name();
    private Integer totalFields = 0;

    public DataEntryAdapter(@NonNull LayoutInflater layoutInflater,
                            @NonNull FragmentManager fragmentManager,
                            @NonNull DataEntryArguments dataEntryArguments) {
        super(new DataEntryDiff());
        setHasStableIds(true);
        rows = new ArrayList<>();
        viewModels = new ArrayList<>();
        processor = PublishProcessor.create();
        sectionProcessor = PublishProcessor.create();
        imageSelector = new ObservableField<>("");
        selectedSection = new ObservableField<>("");
        this.processorOptionSet = PublishProcessor.create();
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
        rows.add(PICTURE, new PictureRow(fragmentManager, layoutInflater, processor, true));
        rows.add(SCAN_CODE, new ScanTextRow(layoutInflater, processor, true));
        rows.add(SECTION, new SectionRow(layoutInflater, selectedSection, sectionProcessor));
        rows.add(OPTION_SET_SELECT, new OptionSetRow(layoutInflater, processor, true, rendering, currentFocusUid));
    }

    public DataEntryAdapter(@NonNull LayoutInflater layoutInflater,
                            @NonNull FragmentManager fragmentManager,
                            @NonNull DataEntryArguments dataEntryArguments,
                            @NonNull FlowableProcessor<RowAction> processor,
                            @NonNull FlowableProcessor<String> sectionProcessor,
                            @NonNull FlowableProcessor<Trio<String, String, Integer>> processorOptSet) {
        super(new DataEntryDiff());
        setHasStableIds(true);
        rows = new ArrayList<>();
        viewModels = new ArrayList<>();
        this.processor = processor;
        this.sectionProcessor = sectionProcessor;
        imageSelector = new ObservableField<>("");
        selectedSection = new ObservableField<>("");
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
        rows.add(PICTURE, new PictureRow(fragmentManager, layoutInflater, processor, true));
        rows.add(SCAN_CODE, new ScanTextRow(layoutInflater, processor, true));
        rows.add(SECTION, new SectionRow(layoutInflater, selectedSection, sectionProcessor));
        rows.add(OPTION_SET_SELECT, new OptionSetRow(layoutInflater, processor, true, rendering, currentFocusUid));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == IMAGE)
            return ((ImageRow) rows.get(IMAGE)).onCreate(parent, totalFields, imageSelector, rendering);
        else
            return rows.get(viewType).onCreate(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        rows.get(holder.getItemViewType()).onBind(holder,
                getItem(position));
        if (!(holder instanceof SectionHolder)) {
            if (!(holder instanceof ImageHolder)) {
                holder.itemView.setBackgroundResource(R.color.form_field_background);
            }
        } else {
            ((SectionHolder) holder).setBottomShadow(
                    position > 0 && getItemViewType(position - 1) != SECTION);
            ((SectionHolder) holder).setLastSectionHeight(
                    position == getItemCount()-1 && getItemViewType(position - 1) != SECTION);
            ((SectionHolder)holder).setSectionNumber(getSectionNumber(position));
        }
    }

    private int getSectionNumber(int sectionPosition){
        int sectionNumber = 1;
        for(int i = 0; i < sectionPosition;i++){
            if(getItemViewType(i) == SECTION){
                sectionNumber++;
            }
        }
        return sectionNumber;
    }

    @Override
    public int getItemViewType(int position) {

        FieldViewModel viewModel = getItem(position);
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
        } else if (viewModel instanceof PictureViewModel) {
            return PICTURE;
        } else if (viewModel instanceof ScanTextViewModel) {
            return SCAN_CODE;
        } else if (viewModel instanceof SectionViewModel) {
            return SECTION;
        } else if (viewModel instanceof OptionSetViewModel) {
            return OPTION_SET_SELECT;
        } else {
            throw new IllegalStateException("Unsupported view model type: "
                    + viewModel.getClass());
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).uid().hashCode();
    }

    @NonNull
    public FlowableProcessor<RowAction> asFlowable() {
        return processor;
    }

    @NonNull
    public FlowableProcessor<String> sectionFlowable() {
        return sectionProcessor;
    }

    public void swap(@NonNull List<FieldViewModel> updates, Runnable commitCallback) {
        sectionPositions = new ArrayList<>();
        rendering = null;
        int imageFields = 0;
        for (FieldViewModel fieldViewModel : updates) {
            if (fieldViewModel instanceof SectionViewModel) {
                sectionPositions.add(viewModels.indexOf(fieldViewModel));
                if (((SectionViewModel) fieldViewModel).isOpen()) {
                    rendering = ((SectionViewModel) fieldViewModel).rendering();
                    totalFields = ((SectionViewModel) fieldViewModel).totalFields();
                }
            } else if (fieldViewModel instanceof ImageViewModel) {
                imageFields++;
            }
        }

        totalFields = imageFields;


        submitList(updates, () -> {
            int currentFocusPosition = -1;
            int lastFocusPosition = -1;

            if (lastFocusItem != null) {
                nextFocusPosition = -1;
                for (int i = 0; i < updates.size(); i++) {
                    if (updates.get(i).uid().equals(lastFocusItem)) {
                        lastFocusPosition = i;
                        nextFocusPosition = i + 1;
                    }
                    if (i == nextFocusPosition && !updates.get(i).editable() && !(updates.get(i) instanceof SectionViewModel)) {
                        nextFocusPosition++;
                    }
                    if (updates.get(i).uid().equals(currentFocusUid.getValue()))
                        currentFocusPosition = i;
                }
            }

            if (nextFocusPosition != -1 && currentFocusPosition == lastFocusPosition && nextFocusPosition < updates.size())
                currentFocusUid.setValue(getItem(nextFocusPosition).uid());
            else if (currentFocusPosition != -1 && currentFocusPosition < updates.size())
                currentFocusUid.setValue(getItem(currentFocusPosition).uid());

            commitCallback.run();
        });


    }

    public void setLastFocusItem(String lastFocusItem) {
        currentFocusUid.setValue(lastFocusItem);
        this.nextFocusPosition = -1;
        this.lastFocusItem = lastFocusItem;
    }

    public int getItemSpan(int position) {

        if (getItemViewType(position) == SECTION || getItemViewType(position) == DISPLAY || rendering == null) {
            return 2;
        } else {
            switch (ProgramStageSectionRenderingType.valueOf(rendering)) {
                case MATRIX:
                    return 1;
                case LISTING:
                case SEQUENTIAL:
                default:
                    return 2;
            }
        }
    }

    public SectionViewModel getSectionAt(int position) {
        return (SectionViewModel) getItem(position);
    }

    public String setCurrentSection(String currentSection) {
        selectedSection.set(currentSection);
        return selectedSection.get();
    }

    public SectionHolder createHeader(FormSectionBinding headerBinding) {
        return new SectionHolder(headerBinding, selectedSection, sectionProcessor);
    }

    public int sectionViewType() {
        return SECTION;
    }
}

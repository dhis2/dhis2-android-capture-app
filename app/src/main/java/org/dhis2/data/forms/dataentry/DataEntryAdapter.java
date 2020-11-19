package org.dhis2.data.forms.dataentry;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.ListAdapter;

import org.dhis2.data.forms.dataentry.fields.FieldUiModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.FormViewHolderFactory;
import org.dhis2.data.forms.dataentry.fields.image.ImageViewModel;
import org.dhis2.data.forms.dataentry.fields.section.SectionHolder;
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class DataEntryAdapter extends ListAdapter<FieldUiModel, FormViewHolder> {

    private final SectionHandler sectionHandler = new SectionHandler();
    private final ObservableField<String> selectedSection;
    private final FlowableProcessor<String> sectionProcessor;

    private final MutableLiveData<String> currentFocusUid;

    private String lastFocusItem;
    private int nextFocusPosition = -1;

    Map<String, Integer> sectionPositions = new LinkedHashMap<>();
    private String rendering = ProgramStageSectionRenderingType.LISTING.name();
    private Integer totalFields = 0;
    private int openSectionPos = 0;
    private String lastOpenedSectionUid = "";

    private String openSection;

    private final FormViewHolderFactory formViewHolderFactory;

    public DataEntryAdapter() {
        super(new DataEntryDiff());
        setHasStableIds(true);
        sectionProcessor = PublishProcessor.create();
        selectedSection = new ObservableField<>("");
        this.currentFocusUid = new MutableLiveData<>();
        this.formViewHolderFactory = new FormViewHolderFactory(
                sectionProcessor,
                selectedSection);
    }

    public DataEntryAdapter(@NonNull FlowableProcessor<String> sectionProcessor) {
        super(new DataEntryDiff());
        setHasStableIds(true);
        this.sectionProcessor = sectionProcessor;
        selectedSection = new ObservableField<>("");
        this.currentFocusUid = new MutableLiveData<>();
        this.formViewHolderFactory = new FormViewHolderFactory(
                sectionProcessor,
                selectedSection);
    }

    @NonNull
    @Override
    public FormViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, viewType, parent, false);
        return Objects.requireNonNull(formViewHolderFactory.provideHolder(binding, viewType));
    }

    @Override
    public void onBindViewHolder(@NonNull FormViewHolder holder, int position) {
        holder.bind(getItem(position));

        if (holder instanceof SectionHolder) {
            updateSectionData((SectionHolder) holder, position, false);
        }
    }


    public void updateSectionData(SectionHolder holder, int position, boolean isHeader) {
        holder.setBottomShadow(!isHeader && position > 0 && getItemViewType(position - 1) != DataEntryViewHolderTypes.SECTION.ordinal());
        if (position > 0) {
            holder.setLastSectionHeight(
                    position == getItemCount() - 1 && getItemViewType(position - 1) != DataEntryViewHolderTypes.SECTION.ordinal());
        }
        holder.setSectionNumber(getSectionNumber(position));
    }

    private int getSectionNumber(int sectionPosition) {
        int sectionNumber = 1;
        for (int i = 0; i < sectionPosition; i++) {
            if (getItemViewType(i) == DataEntryViewHolderTypes.SECTION.ordinal()) {
                sectionNumber++;
            }
        }
        return sectionNumber;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getLayoutId();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getUid().hashCode();
    }

    @NonNull
    public FlowableProcessor<String> sectionFlowable() {
        return sectionProcessor;
    }

    public void swap(@NonNull List<FieldViewModel> updates, Runnable commitCallback) {
        sectionPositions = new LinkedHashMap<>();
        rendering = null;
        int imageFields = 0;
        List<FieldUiModel> items = new ArrayList<>();
        for (FieldViewModel fieldViewModel : updates) {
            if (fieldViewModel instanceof SectionViewModel) {
                sectionPositions.put(fieldViewModel.getUid(), updates.indexOf(fieldViewModel));
                if (((SectionViewModel) fieldViewModel).isOpen()) {
                    rendering = ((SectionViewModel) fieldViewModel).rendering();
                    totalFields = ((SectionViewModel) fieldViewModel).totalFields();
                    setOpenSectionPos(updates.indexOf(fieldViewModel), fieldViewModel.getUid());
                } else if (fieldViewModel.getUid().equals(lastOpenedSectionUid)) {
                    openSectionPos = -1;
                }
            } else if (fieldViewModel instanceof ImageViewModel) {
                imageFields++;
            }

            items.add(fieldViewModel);
        }

        totalFields = imageFields;

        submitList(items, () -> {
            int currentFocusPosition = -1;
            int lastFocusPosition = -1;

            if (lastFocusItem != null) {
                nextFocusPosition = -1;
                for (int i = 0; i < items.size(); i++) {

                    FieldViewModel item = (FieldViewModel) items.get(i);

                    if (item.getUid().equals(lastFocusItem)) {
                        lastFocusPosition = i;
                        nextFocusPosition = i + 1;
                    }
                    if (i == nextFocusPosition && !item.editable() && !(item instanceof SectionViewModel)) {
                        nextFocusPosition++;
                    }
                    if (item.getUid().equals(currentFocusUid.getValue()))
                        currentFocusPosition = i;
                }
            }

            if (nextFocusPosition != -1 && currentFocusPosition == lastFocusPosition && nextFocusPosition < items.size())
                currentFocusUid.setValue(getItem(nextFocusPosition).getUid());
            else if (currentFocusPosition != -1 && currentFocusPosition < items.size())
                currentFocusUid.setValue(getItem(currentFocusPosition).getUid());

            commitCallback.run();
        });
    }

    public void setLastFocusItem(String lastFocusItem) {
        currentFocusUid.setValue(lastFocusItem);
        this.nextFocusPosition = -1;
        this.lastFocusItem = lastFocusItem;
    }

    public int getItemSpan(int position) {

        if (position >= getItemCount() ||
                getItemViewType(position) == DataEntryViewHolderTypes.SECTION.ordinal() ||
                getItemViewType(position) == DataEntryViewHolderTypes.DISPLAY.ordinal() ||
                rendering == null
        ) {
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

    public String setCurrentSection(String currentSection) {
        selectedSection.set(currentSection);
        return selectedSection.get();
    }

    public void saveOpenedSection(String openSectionUid) {
        this.openSection = openSectionUid;
    }

    private void setOpenSectionPos(int sectionOpened, String openSectionUid) {
        lastOpenedSectionUid = openSectionUid;
        openSectionPos = sectionOpened;
    }

    public int getSavedPosition() {
        if (TextUtils.isEmpty(openSection))
            return -1;
        else {
            return sectionPositions.get(openSection);
        }
    }

    public int getSectionSize() {
        return sectionPositions.size();
    }

    @Nullable
    public SectionViewModel getSectionForPosition(int visiblePos) {
        int sectionPosition = sectionHandler.getSectionPositionFromVisiblePosition(
                visiblePos,
                isSection(visiblePos),
                new ArrayList<>(sectionPositions.values()));
        if (sectionPosition != -1) {
            return (SectionViewModel) getItem(sectionPosition);
        } else {
            return null;
        }
    }

    public int getSectionPosition(String sectionUid) {
        return sectionPositions.get(sectionUid);
    }

    public boolean isSection(int position) {
        if (position <= getItemCount()) {
            return getItemViewType(position) == DataEntryViewHolderTypes.SECTION.ordinal();
        } else {
            return false;
        }
    }
}

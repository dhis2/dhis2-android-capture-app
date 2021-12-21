package org.dhis2.data.forms.dataentry.fields.section;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.FieldUiModel;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.util.Objects;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

@AutoValue
public abstract class SectionViewModel extends FieldViewModel {

    public static final String CLOSING_SECTION_UID = "closing_section";
    private boolean showBottomShadow;
    private boolean lastPositionShouldChangeHeight;

    public abstract boolean isOpen();

    @NonNull
    public abstract Integer totalFields();

    @NonNull
    public abstract Integer completedFields();

    @Nullable
    public abstract Integer errors();

    @Nullable
    public abstract Integer warnings();

    @NonNull
    public abstract String rendering();

    @Nullable
    public abstract FlowableProcessor<String> sectionProcessor();

    @Nullable
    public abstract ObservableField<String> selectedField();


    private int sectionNumber;

    public static SectionViewModel create(String sectionUid, int layoutId, String sectionName, String description, boolean isOpen, Integer totalFields, Integer completedFields, String rendering, FlowableProcessor<String> sectionProcessor, ObservableField<String> currentSection) {
        return new AutoValue_SectionViewModel(
                sectionUid,
                layoutId,
                sectionName,
                false,
                null,
                null,
                false,
                false,
                null,
                null,
                null,
                description,
                ObjectStyle.builder().build(),
                null,
                DataEntryViewHolderTypes.SECTION,
                null,
                null,
                false,
                null,
                isOpen,
                totalFields,
                completedFields,
                null,
                null,
                rendering != null ? rendering : ProgramStageSectionRenderingType.LISTING.name(),
                sectionProcessor,
                currentSection
        );
    }

    public static SectionViewModel createClosingSection() {
        return new AutoValue_SectionViewModel(
                SectionViewModel.CLOSING_SECTION_UID,
                R.layout.form_section,
                SectionViewModel.CLOSING_SECTION_UID,
                false,
                null,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                ObjectStyle.builder().build(),
                null,
                DataEntryViewHolderTypes.SECTION,
                null,
                null,
                false,
                null,
                false,
                0,
                0,
                null,
                null,
                ProgramStageSectionRenderingType.LISTING.name(),
                PublishProcessor.create(),
                new ObservableField("")
        );
    }

    @Override
    public FieldViewModel setMandatory() {
        return this;
    }

    @NonNull
    public SectionViewModel withErrors(@Nullable Integer errors) {
        return new AutoValue_SectionViewModel(
                uid(),
                layoutId(),
                label(),
                false,
                null,
                null,
                false,
                false,
                null,
                null,
                null,
                description(),
                objectStyle(),
                null,
                DataEntryViewHolderTypes.SECTION,
                style(),
                hint(),
                activated(),
                valueType(),
                url(),
                isOpen(),
                totalFields(),
                completedFields(),
                errors,
                warnings(),
                rendering(),
                sectionProcessor(),
                selectedField()
        );
    }

    @NonNull
    public SectionViewModel withErrorsAndWarnings(@Nullable Integer errors, @Nullable Integer warnings) {
        return new AutoValue_SectionViewModel(
                uid(),
                layoutId(),
                label(),
                false,
                null,
                null,
                false,
                false,
                null,
                null,
                null,
                description(),
                objectStyle(),
                null,
                DataEntryViewHolderTypes.SECTION,
                style(),
                hint(),
                activated(),
                valueType(),
                url(),
                isOpen(),
                totalFields(),
                completedFields(),
                errors,
                warnings,
                rendering(),
                sectionProcessor(),
                selectedField()
        );
    }

    @NonNull
    public SectionViewModel withWarnings(@Nullable Integer warnings) {
        return new AutoValue_SectionViewModel(
                uid(),
                layoutId(),
                label(),
                false,
                null,
                null,
                false,
                false,
                null,
                null,
                null,
                description(),
                objectStyle(),
                null,
                DataEntryViewHolderTypes.SECTION,
                style(),
                hint(),
                activated(),
                valueType(),
                url(),
                isOpen(),
                totalFields(),
                completedFields(),
                errors(),
                warnings,
                rendering(),
                sectionProcessor(),
                selectedField()
        );
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return this;
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return this;
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return this;
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return this;
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return this;
    }

    public SectionViewModel setOpen(boolean isOpen) {
        return new AutoValue_SectionViewModel(
                uid(),
                layoutId(),
                label(),
                false,
                null,
                null,
                false,
                false,
                null,
                null,
                error(),
                description(),
                objectStyle(),
                null,
                DataEntryViewHolderTypes.SECTION,
                style(),
                hint(),
                activated(),
                valueType(),
                url(),
                isOpen,
                totalFields(),
                completedFields(),
                errors(),
                warnings(),
                rendering(),
                sectionProcessor(),
                selectedField()
        );
    }

    public SectionViewModel setTotalFields(Integer totalFields) {
        return new AutoValue_SectionViewModel(
                uid(),
                layoutId(),
                label(),
                false,
                null,
                null,
                false,
                false,
                null,
                null,
                error(),
                description(),
                objectStyle(),
                null,
                DataEntryViewHolderTypes.SECTION,
                style(),
                hint(),
                activated(),
                valueType(),
                url(),
                isOpen(),
                totalFields,
                completedFields(),
                errors(),
                warnings(),
                rendering(),
                sectionProcessor(),
                selectedField()
        );
    }

    public SectionViewModel setCompletedFields(Integer completedFields) {
        return new AutoValue_SectionViewModel(
                uid(),
                layoutId(),
                label(),
                false,
                null,
                null,
                false,
                false,
                null,
                null,
                error(),
                description(),
                objectStyle(),
                null,
                DataEntryViewHolderTypes.SECTION,
                style(),
                hint(),
                activated(),
                valueType(),
                url(),
                isOpen(),
                totalFields(),
                completedFields,
                errors(),
                warnings(),
                rendering(),
                sectionProcessor(),
                selectedField()
        );
    }

    public boolean hasToShowDescriptionIcon(boolean isTitleEllipsized) {
        return (description() != null && !Objects.requireNonNull(description()).isEmpty()) ||
                isTitleEllipsized;
    }

    public boolean isClosingSection() {
        return uid().equals(CLOSING_SECTION_UID);
    }

    public boolean hasErrorAndWarnings() {
        return (errors() != null && warnings() != null);
    }

    public boolean hasNotAnyErrorOrWarning() {
        return (errors() == null && warnings() == null);
    }

    public boolean hasOnlyErrors() {
        return (errors() != null && warnings() == null);
    }

    public String getFormattedSectionFieldsInfo() {
        return String.format("%s/%s", completedFields(), totalFields());
    }

    public boolean areAllFieldsCompleted() {
        return completedFields().equals(totalFields());
    }

    public void setSelected() {
        onItemClick();

        if (selectedField() != null && sectionProcessor() != null) {
            String sectionToOpen = Objects.equals(selectedField().get(), uid()) ? "" : uid();
            selectedField().set(sectionToOpen);
            sectionProcessor().onNext(sectionToOpen);
        }
    }

    public void setSectionNumber(int sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public int getSectionNumber() {
        return sectionNumber;
    }

    public boolean isSelected() {
        if (selectedField() == null) {
            return false;
        } else {
            return Objects.equals(selectedField().get(), uid());
        }
    }

    public void setShowBottomShadow(boolean showBottomShadow) {
        this.showBottomShadow = showBottomShadow;
    }

    public boolean showBottomShadow() {
        return showBottomShadow;
    }

    public void setLastSectionHeight(boolean lastPositionShouldChangeHeight) {
        this.lastPositionShouldChangeHeight = lastPositionShouldChangeHeight;
    }

    public boolean lastPositionShouldChangeHeight() {
        return lastPositionShouldChangeHeight;
    }

    @Override
    public boolean equals(FieldUiModel o) {
        return super.equals(o) &&
                o instanceof SectionViewModel &&
                this.showBottomShadow == ((SectionViewModel) o).showBottomShadow() &&
                this.lastPositionShouldChangeHeight == ((SectionViewModel) o).lastPositionShouldChangeHeight() &&
                this.isOpen() == ((SectionViewModel) o).isOpen() &&
                this.totalFields().equals(((SectionViewModel) o).totalFields()) &&
                this.completedFields().equals(((SectionViewModel) o).completedFields()) &&
                Objects.equals(this.errors(), ((SectionViewModel) o).errors()) &&
                Objects.equals(this.warnings(), ((SectionViewModel) o).warnings()) &&
                this.rendering().equals(((SectionViewModel) o).rendering()) &&
                this.sectionProcessor() == ((SectionViewModel) o).sectionProcessor() &&
                this.selectedField() == ((SectionViewModel) o).selectedField() &&
                this.sectionNumber == ((SectionViewModel) o).sectionNumber;
    }
}

package org.dhis2.data.forms.dataentry.fields.section;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.util.Objects;

@AutoValue
public abstract class SectionViewModel extends FieldViewModel {

    public static final String CLOSING_SECTION_UID = "closing_section";
    private boolean showBottomShadow;
    private boolean lastPositionShouldChangeHeight;

    @NonNull
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

    private int sectionNumber;
    private ObservableField<String> selectedField;

    public static SectionViewModel create(String sectionUid, String sectionName, String description, boolean isOpen, Integer totalFields, Integer completedFields, String rendering) {
        return new AutoValue_SectionViewModel(
                sectionUid,
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
                isOpen,
                totalFields,
                completedFields,
                null,
                null,
                rendering != null ? rendering : ProgramStageSectionRenderingType.LISTING.name()
        );
    }

    public static SectionViewModel createClosingSection() {
        return new AutoValue_SectionViewModel(
                SectionViewModel.CLOSING_SECTION_UID,
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
                false,
                0,
                0,
                null,
                null,
                ProgramStageSectionRenderingType.LISTING.name()
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
                isOpen(),
                totalFields(),
                completedFields(),
                errors,
                warnings(),
                rendering()

        );
    }

    @NonNull
    public SectionViewModel withErrorsAndWarnings(@Nullable Integer errors, @Nullable Integer warnings) {
        return new AutoValue_SectionViewModel(
                uid(),
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
                isOpen(),
                totalFields(),
                completedFields(),
                errors,
                warnings,
                rendering()

        );
    }

    @NonNull
    public SectionViewModel withWarnings(@Nullable Integer warnings) {
        return new AutoValue_SectionViewModel(
                uid(),
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
                isOpen(),
                totalFields(),
                completedFields(),
                errors(),
                warnings,
                rendering()

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

    public SectionViewModel setOpen(boolean isOpen) {
        return new AutoValue_SectionViewModel(
                uid(),
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
                isOpen,
                totalFields(),
                completedFields(),
                errors(),
                warnings(),
                rendering()
        );
    }

    public SectionViewModel setTotalFields(Integer totalFields) {
        return new AutoValue_SectionViewModel(
                uid(),
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
                isOpen(),
                totalFields,
                completedFields(),
                errors(),
                warnings(),
                rendering()
        );
    }

    public SectionViewModel setCompletedFields(Integer completedFields) {
        return new AutoValue_SectionViewModel(
                uid(),
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
                isOpen(),
                totalFields(),
                completedFields,
                errors(),
                warnings(),
                rendering()
        );
    }

    public boolean hasToShowDescriptionIcon(boolean isTitleEllipsized) {
        return (description() != null && !Objects.requireNonNull(description()).isEmpty()) ||
                isTitleEllipsized;
    }

    @Override
    public int getLayoutId() {
        return R.layout.form_section;
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

    }

    public void setSectionNumber(int sectionNumber){
        this.sectionNumber = sectionNumber;
    }

    public int getSectionNumber() {
        return sectionNumber;
    }

    public void setSelectedField(ObservableField<String> selectedObservableField){
        this.selectedField = selectedObservableField;
    }
    public ObservableField<String> observeSelectedSection() {
        return selectedField==null?new ObservableField<>(""):selectedField;
    }

    public boolean isSelected() {
        return Objects.equals(selectedField.get(), uid());
    }

    public void setShowBottomShadow(boolean showBottomShadow){
        this.showBottomShadow = showBottomShadow;
    }
    public boolean showBottomShadow(){
        return showBottomShadow;
    }
    public void setLastSectionHeight(boolean lastPositionShouldChangeHeight){
        this.lastPositionShouldChangeHeight = lastPositionShouldChangeHeight;
    }

    public boolean lastPositionShouldChangeHeight(){
        return lastPositionShouldChangeHeight;
    }
}

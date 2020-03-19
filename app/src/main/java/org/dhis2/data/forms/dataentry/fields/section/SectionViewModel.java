package org.dhis2.data.forms.dataentry.fields.section;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@AutoValue
public abstract class SectionViewModel extends FieldViewModel {

    @NonNull
    public abstract boolean isOpen();

    @NonNull
    public abstract Integer totalFields();

    @NonNull
    public abstract Integer completedFields();

    @Nullable
    public abstract Integer errors();

    @NonNull
    public abstract String rendering();

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
                isOpen,
                totalFields,
                completedFields,
                null,
                rendering != null ? rendering : ProgramStageSectionRenderingType.LISTING.name()
        );
    }

    @Override
    public FieldViewModel setMandatory() {
        return this;
    }

    @NonNull
    public SectionViewModel withErrors(@NonNull Integer errors) {
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
                isOpen(),
                totalFields(),
                completedFields(),
                errors,
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

    @Nonnull
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
                isOpen,
                totalFields(),
                completedFields(),
                errors(),
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
                isOpen(),
                totalFields,
                completedFields(),
                errors(),
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
                isOpen(),
                totalFields(),
                completedFields,
                errors(),
                rendering()
        );
    }

}

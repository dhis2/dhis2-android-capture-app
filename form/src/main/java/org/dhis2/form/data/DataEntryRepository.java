package org.dhis2.form.data;

import androidx.annotation.NonNull;

import org.dhis2.form.model.FieldUiModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import io.reactivex.Flowable;

public interface DataEntryRepository {
    @NonNull
    Flowable<List<FieldUiModel>> list();

    @NotNull Flowable<List<String>> sectionUids();

    FieldUiModel updateSection(@NonNull FieldUiModel sectionToUpdate,
                               boolean isSectionOpen,
                               int totalFields,
                               int fieldsWithValue,
                               int errorCount,
                               int warningCount);

    FieldUiModel updateField(@NonNull FieldUiModel fieldUiModel,
                             @Nullable String warningMessage,
                             @NonNull List<String> optionsToHide,
                             @NonNull List<String> optionGroupsToHide,
                             @NonNull List<String> optionGroupsToShow);

    boolean isEvent();
}

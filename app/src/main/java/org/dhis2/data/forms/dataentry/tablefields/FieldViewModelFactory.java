package org.dhis2.data.forms.dataentry.tablefields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.composetable.model.DropdownOption;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.program.SectionRenderingType;

import java.util.List;

public interface FieldViewModelFactory {

    @NonNull
    FieldViewModel create(@NonNull String id,
                          @NonNull String label,
                          @NonNull ValueType valueType,
                          @NonNull Boolean mandatory,
                          @Nullable String optionSet,
                          @Nullable String value,
                          @Nullable String programStageSection,
                          @Nullable Boolean AllowFutureDate,
                          @NonNull Boolean editable,
                          @Nullable SectionRenderingType renderingType,
                          @Nullable String description,
                          @Nullable String dataElement,
                          @Nullable List<String> listCategoryOption,
                          @Nullable String storeBy,
                          @Nullable int row,
                          @Nullable int column,
                          @Nullable String categoryOptionCombo,
                          @Nullable String catCombo,
                          @Nullable List<DropdownOption> options);
}

package org.dhis2.data.forms.dataentry.fields;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRenderingModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

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
                          @Nullable ProgramStageSectionRenderingType renderingType,
                          @Nullable String description,
                          @Nullable ValueTypeDeviceRenderingModel fieldRendering);
}

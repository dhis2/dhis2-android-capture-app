package org.dhis2.data.forms.dataentry.fields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.LegendValue;
import org.dhis2.form.model.RowAction;
import org.dhis2.form.ui.style.BasicFormUiModelStyle;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;

import java.util.List;

import autovalue.shaded.org.checkerframework$.checker.nullness.qual.$NonNull;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;

public interface FieldViewModelFactory {

    @NonNull
    FieldUiModel create(@NonNull String id,
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
                        @Nullable ValueTypeDeviceRendering fieldRendering,
                        @Nullable Integer optionCount,
                        @NonNull ObjectStyle objectStyle,
                        @Nullable String fieldMask,
                        @Nullable LegendValue legendValue,
                        List<Option> options,
                        @Nullable FeatureType featureType
    );

    @NonNull
    FieldUiModel createForAttribute(@$NonNull TrackedEntityAttribute trackedEntityAttribute,
                                    @Nullable ProgramTrackedEntityAttribute programTrackedEntityAttribute,
                                    @Nullable String value,
                                    boolean editable,
                                    List<Option> options);

    @NonNull
    FieldUiModel createSingleSection(String singleSectionName);

    @NonNull
    FieldUiModel createSection(String sectionUid, String sectionName, String description,
                               boolean isOpen, int totalFields, int completedFields, String rendering);

    @NonNull
    FieldUiModel createClosingSection();

    @NonNull
    Flowable<String> sectionProcessor();

    @NonNull
    FlowableProcessor<RowAction> fieldProcessor();
}

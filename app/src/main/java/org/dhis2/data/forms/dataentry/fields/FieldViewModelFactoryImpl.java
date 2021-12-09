package org.dhis2.data.forms.dataentry.fields;

import static org.dhis2.commons.extensions.Preconditions.isNull;
import static org.dhis2.form.model.SectionUiModelImpl.CLOSING_SECTION_UID;
import static org.dhis2.form.model.SectionUiModelImpl.SINGLE_SECTION_UID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;

import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.FieldUiModelImpl;
import org.dhis2.form.model.LegendValue;
import org.dhis2.form.model.RowAction;
import org.dhis2.form.model.SectionUiModelImpl;
import org.dhis2.form.ui.event.UiEventFactoryImpl;
import org.dhis2.form.ui.provider.DisplayNameProvider;
import org.dhis2.form.ui.provider.HintProvider;
import org.dhis2.form.ui.provider.KeyboardActionProvider;
import org.dhis2.form.ui.provider.LayoutProvider;
import org.dhis2.form.ui.provider.UiEventTypesProvider;
import org.dhis2.form.ui.provider.UiStyleProvider;
import org.dhis2.utils.DhisTextUtils;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import autovalue.shaded.org.checkerframework$.checker.nullness.qual.$NonNull;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import kotlin.jvm.JvmClassMappingKt;

public final class FieldViewModelFactoryImpl implements FieldViewModelFactory {

    @NonNull
    private final Map<ValueType, String> valueTypeHintMap;

    private final FlowableProcessor<RowAction> fieldProcessor = PublishProcessor.create();
    private final FlowableProcessor<String> sectionProcessor = PublishProcessor.create();
    private final ObservableField<String> currentSection = new ObservableField<>("");

    private final List<ValueTypeRenderingType> optionSetTextRenderings = Arrays.asList(
            ValueTypeRenderingType.HORIZONTAL_CHECKBOXES,
            ValueTypeRenderingType.VERTICAL_CHECKBOXES,
            ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS,
            ValueTypeRenderingType.VERTICAL_RADIOBUTTONS
    );
    private final boolean searchMode;
    private final LayoutProvider layoutProvider;
    private final HintProvider hintProvider;
    private final DisplayNameProvider displayNameProvider;
    private final UiEventTypesProvider uiEventTypesProvider;
    private final KeyboardActionProvider keyboardActionProvider;
    private final UiStyleProvider uiStyleProvider;

    public FieldViewModelFactoryImpl(
            @NonNull Map<ValueType, String> valueTypeHintMap,
            boolean searchMode,
            UiStyleProvider uiStyleProvider,
            LayoutProvider layoutProvider,
            HintProvider hintProvider,
            DisplayNameProvider displayNameProvider,
            UiEventTypesProvider uiEventTypesProvider,
            KeyboardActionProvider keyboardActionProvider) {
        this.valueTypeHintMap = valueTypeHintMap;
        this.searchMode = searchMode;
        this.uiStyleProvider = uiStyleProvider;
        this.layoutProvider = layoutProvider;
        this.hintProvider = hintProvider;
        this.displayNameProvider = displayNameProvider;
        this.uiEventTypesProvider = uiEventTypesProvider;
        this.keyboardActionProvider = keyboardActionProvider;
    }

    @NonNull
    @Override
    public FieldUiModel createForAttribute(@$NonNull TrackedEntityAttribute trackedEntityAttribute,
                                           @Nullable ProgramTrackedEntityAttribute programTrackedEntityAttribute,
                                           @Nullable String value,
                                           boolean editable,
                                           List<Option> options) {
        return create(trackedEntityAttribute.uid(),
                trackedEntityAttribute.displayFormName(),
                trackedEntityAttribute.valueType(),
                programTrackedEntityAttribute != null ? programTrackedEntityAttribute.mandatory() : false,
                trackedEntityAttribute.optionSet() != null ? trackedEntityAttribute.optionSet().uid() : null,
                value,
                null,
                programTrackedEntityAttribute != null && programTrackedEntityAttribute.allowFutureDate() != null ? programTrackedEntityAttribute.allowFutureDate() : true,
                editable,
                ProgramStageSectionRenderingType.LISTING,
                programTrackedEntityAttribute != null ? programTrackedEntityAttribute.displayDescription() : trackedEntityAttribute.displayDescription(),
                programTrackedEntityAttribute != null && programTrackedEntityAttribute.renderType() != null ? programTrackedEntityAttribute.renderType().mobile() : null,
                null,
                trackedEntityAttribute.style() != null ? trackedEntityAttribute.style() : ObjectStyle.builder().build(),
                trackedEntityAttribute.fieldMask(),
                null,
                options,
                trackedEntityAttribute.valueType() == ValueType.COORDINATE ? FeatureType.POINT : null);
    }

    @NonNull
    @Override
    public FieldUiModel create(@NonNull String id,
                               @NonNull String label,
                               @NonNull ValueType type,
                               @NonNull Boolean mandatory,
                               @Nullable String optionSet,
                               @Nullable String value,
                               @Nullable String section,
                               @Nullable Boolean allowFutureDates,
                               @NonNull Boolean editable,
                               @Nullable ProgramStageSectionRenderingType renderingType,
                               @Nullable String description,
                               @Nullable ValueTypeDeviceRendering fieldRendering,
                               @Nullable Integer optionCount,
                               ObjectStyle objectStyle,
                               @Nullable String fieldMask,
                               @Nullable LegendValue legendValue,
                               @NonNull List<Option> options,
                               @Nullable FeatureType featureType) {
        isNull(type, "type must be supplied");
        if (searchMode)
            mandatory = false;
        if (DhisTextUtils.Companion.isNotEmpty(optionSet)) {
            if (renderingType == null || renderingType == ProgramStageSectionRenderingType.LISTING) {
                ValueTypeRenderingType valueTypeRenderingType = fieldRendering != null ? fieldRendering.type() : ValueTypeRenderingType.DEFAULT;
                if (valueTypeRenderingType.equals(ValueTypeRenderingType.QR_CODE) || valueTypeRenderingType.equals(ValueTypeRenderingType.BAR_CODE)) {
                } else {
                    return new FieldUiModelImpl(
                            id,
                            layoutProvider.getLayoutByType(type, valueTypeRenderingType, optionSet, renderingType),
                            value,
                            false,
                            null,
                            editable,
                            null,
                            mandatory,
                            label,
                            section,
                            uiStyleProvider.provideStyle(type),
                            hintProvider.provideDateHint(type),
                            description,
                            type,
                            legendValue,
                            optionSet,
                            allowFutureDates,
                            new UiEventFactoryImpl(id, label, description, type, allowFutureDates),
                            displayNameProvider.provideDisplayName(type, value, optionSet),
                            uiEventTypesProvider.provideUiRenderType(featureType, valueTypeRenderingType, renderingType),
                            options,
                            keyboardActionProvider.provideKeyboardAction(type),
                            fieldMask
                    );
                }
            }
        }
        return new FieldUiModelImpl(
                id,
                layoutProvider.getLayoutByType(type, fieldRendering != null ? fieldRendering.type() : null, optionSet, renderingType),
                value,
                false,
                null,
                editable,
                null,
                mandatory,
                label,
                section,
                uiStyleProvider.provideStyle(type),
                hintProvider.provideDateHint(type),
                description,
                type,
                legendValue,
                optionSet,
                allowFutureDates,
                new UiEventFactoryImpl(id, label, description, type, allowFutureDates, optionSet, editable),
                displayNameProvider.provideDisplayName(type, value, optionSet),
                uiEventTypesProvider.provideUiRenderType(featureType, fieldRendering != null ? fieldRendering.type() : null, renderingType),
                options,
                keyboardActionProvider.provideKeyboardAction(type),
                fieldMask
        );
    }

    @NonNull
    @Override
    public FieldUiModel createSingleSection(String singleSectionName) {
        return new SectionUiModelImpl(
                SINGLE_SECTION_UID,
                layoutProvider.getLayoutForSection(),
                null,
                false,
                null,
                false,
                null,
                false,
                singleSectionName,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                0,
                0,
                0,
                0,
                ProgramStageSectionRenderingType.LISTING.name(),
                currentSection
        );
    }

    @NonNull
    @Override
    public FieldUiModel createSection(String sectionUid, String sectionName, String description,
                                      boolean isOpen, int totalFields, int completedFields, String rendering) {
        return new SectionUiModelImpl(
                sectionUid,
                layoutProvider.getLayoutForSection(),
                null,
                false,
                null,
                false,
                null,
                false,
                sectionName,
                null,
                null,
                null,
                description,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                isOpen,
                totalFields,
                completedFields,
                0,
                0,
                rendering,
                currentSection
        );
    }

    @NonNull
    @Override
    public FieldUiModel createClosingSection() {
        return new SectionUiModelImpl(
                CLOSING_SECTION_UID,
                layoutProvider.getLayoutForSection(),
                null,
                false,
                null,
                false,
                null,
                false,
                CLOSING_SECTION_UID,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                0,
                0,
                0,
                0,
                ProgramStageSectionRenderingType.LISTING.name(),
                currentSection
        );
    }

    @NonNull
    @Override
    public Flowable<String> sectionProcessor() {
        return sectionProcessor;
    }

    @NonNull
    @Override
    public FlowableProcessor<RowAction> fieldProcessor() {
        return fieldProcessor;
    }

    private int getLayout(Class type) {
        return layoutProvider.getLayoutByModel(JvmClassMappingKt.getKotlinClass(type));
    }
}

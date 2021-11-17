package org.dhis2.data.forms.dataentry.fields;

import static org.dhis2.data.forms.dataentry.EnrollmentRepository.SINGLE_SECTION_UID;
import static org.dhis2.utils.Preconditions.isNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;

import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel;
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextViewModel;
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedViewModel;
import org.dhis2.data.forms.dataentry.fields.visualOptionSet.MatrixOptionSetModel;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.FieldUiModelImpl;
import org.dhis2.form.model.LegendValue;
import org.dhis2.form.model.RowAction;
import org.dhis2.form.ui.event.UiEventFactoryImpl;
import org.dhis2.form.ui.provider.DisplayNameProvider;
import org.dhis2.form.ui.provider.HintProvider;
import org.dhis2.form.ui.provider.KeyboardActionProvider;
import org.dhis2.form.ui.provider.LayoutProvider;
import org.dhis2.form.ui.provider.UiEventTypesProvider;
import org.dhis2.form.ui.style.BasicFormUiModelStyle;
import org.dhis2.form.ui.style.FormUiColorFactory;
import org.dhis2.form.ui.style.FormUiModelStyle;
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
import kotlin.reflect.KClass;

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
    private final FormUiColorFactory colorFactory;
    private final LayoutProvider layoutProvider;
    private final HintProvider hintProvider;
    private final DisplayNameProvider displayNameProvider;
    private final UiEventTypesProvider uiEventTypesProvider;
    private final KeyboardActionProvider keyboardActionProvider;

    public FieldViewModelFactoryImpl(
            @NonNull Map<ValueType, String> valueTypeHintMap,
            boolean searchMode,
            FormUiColorFactory colorFactory,
            LayoutProvider layoutProvider,
            HintProvider hintProvider,
            DisplayNameProvider displayNameProvider,
            UiEventTypesProvider uiEventTypesProvider,
            KeyboardActionProvider keyboardActionProvider) {
        this.valueTypeHintMap = valueTypeHintMap;
        this.searchMode = searchMode;
        this.colorFactory = colorFactory;
        this.layoutProvider = layoutProvider;
        this.hintProvider = hintProvider;
        this.displayNameProvider = displayNameProvider;
        this.uiEventTypesProvider = uiEventTypesProvider;
        this.keyboardActionProvider = keyboardActionProvider;
    }

    @Nullable
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
                FeatureType.POINT);
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
        FormUiModelStyle style = new BasicFormUiModelStyle(colorFactory, type);

        final KClass<?> myKClass = JvmClassMappingKt.getKotlinClass(ScanTextViewModel.class);

        if (searchMode)
            mandatory = false;
        if (DhisTextUtils.Companion.isNotEmpty(optionSet)) {
            if (renderingType == null || renderingType == ProgramStageSectionRenderingType.LISTING) {
                if (fieldRendering != null && (fieldRendering.type().equals(ValueTypeRenderingType.QR_CODE) || fieldRendering.type().equals(ValueTypeRenderingType.BAR_CODE))) {
                    return ScanTextViewModel.create(
                            id,
                            getLayout(ScanTextViewModel.class),
                            label,
                            mandatory,
                            value,
                            section,
                            editable,
                            optionSet,
                            description,
                            objectStyle,
                            fieldRendering,
                            valueTypeHintMap.get(type),
                            !searchMode,
                            searchMode,
                            style,
                            type
                    );
                } else if (fieldRendering != null && type == ValueType.TEXT && optionSetTextRenderings.contains(fieldRendering.type())) {
                    return new FieldUiModelImpl(
                            id,
                            getLayoutByValueType(type, fieldRendering.type()),
                            value,
                            false,
                            null,
                            editable,
                            null,
                            mandatory,
                            label,
                            section,
                            style,
                            hintProvider.provideDateHint(type),
                            description,
                            type,
                            legendValue,
                            optionSet,
                            allowFutureDates,
                            new UiEventFactoryImpl(id, label, description, type, allowFutureDates),
                            displayNameProvider.provideDisplayName(type, value, optionSet),
                            uiEventTypesProvider.provideUiRenderType(fieldRendering.type()),
                            options,
                            keyboardActionProvider.provideKeyboardAction(type),
                            fieldMask
                    );
                } else {
                    return SpinnerViewModel.create(
                            id,
                            getLayout(SpinnerViewModel.class),
                            label,
                            valueTypeHintMap.get(type),
                            mandatory,
                            optionSet,
                            value,
                            section,
                            editable,
                            description,
                            objectStyle,
                            !searchMode,
                            ProgramStageSectionRenderingType.LISTING.toString(),
                            legendValue,
                            type
                    );
                }
            } else {
                return MatrixOptionSetModel.create(
                        id,
                        getLayout(MatrixOptionSetModel.class),
                        label,
                        mandatory,
                        value,
                        section,
                        editable,
                        optionSet,
                        description,
                        objectStyle,
                        options,
                        renderingType == ProgramStageSectionRenderingType.MATRIX ? 2 : 1,
                        type
                );
            }
        }

        switch (type) {
            case BOOLEAN:
            case TRUE_ONLY:
                ValueTypeRenderingType valueTypeRenderingType = fieldRendering != null ? fieldRendering.type() : ValueTypeRenderingType.DEFAULT;
                return RadioButtonViewModel.fromRawValue(
                        id,
                        getLayoutByValueType(type, valueTypeRenderingType),
                        label,
                        type,
                        mandatory,
                        value,
                        section,
                        editable,
                        description,
                        objectStyle,
                        valueTypeRenderingType,
                        !searchMode,
                        searchMode
                );
            case FILE_RESOURCE:
            case TRACKER_ASSOCIATE:
            case USERNAME:
                return UnsupportedViewModel.create(
                        id,
                        getLayout(UnsupportedViewModel.class),
                        label,
                        mandatory,
                        value,
                        section,
                        editable,
                        description,
                        objectStyle,
                        type
                );
            default:
                return new FieldUiModelImpl(
                        id,
                        getLayoutByValueType(type, null),
                        value,
                        false,
                        null,
                        editable,
                        null,
                        mandatory,
                        label,
                        section,
                        style,
                        hintProvider.provideDateHint(type),
                        description,
                        type,
                        legendValue,
                        optionSet,
                        allowFutureDates,
                        new UiEventFactoryImpl(id, label, description, type, allowFutureDates),
                        displayNameProvider.provideDisplayName(type, value, optionSet),
                        uiEventTypesProvider.provideUiRenderType(featureType),
                        options,
                        keyboardActionProvider.provideKeyboardAction(type),
                        fieldMask
                );
        }
    }

    @NonNull
    @Override
    public FieldUiModel createSingleSection(String singleSectionName) {
        return SectionViewModel.create(
                SINGLE_SECTION_UID,
                getLayout(SectionViewModel.class),
                singleSectionName,
                null,
                false,
                0,
                0,
                ProgramStageSectionRenderingType.LISTING.name(),
                sectionProcessor,
                currentSection
        );
    }

    @NonNull
    @Override
    public FieldUiModel createSection(String sectionUid, String sectionName, String description,
                                      boolean isOpen, int totalFields, int completedFields, String rendering) {
        return SectionViewModel.create(
                sectionUid,
                getLayout(SectionViewModel.class),
                sectionName,
                description,
                isOpen,
                totalFields,
                completedFields,
                rendering,
                sectionProcessor,
                currentSection
        );
    }

    @NonNull
    @Override
    public FieldUiModel createClosingSection() {
        return SectionViewModel.createClosingSection();
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

    private int getLayoutByValueType(ValueType valueType, ValueTypeRenderingType valueTypeRenderingType) {
        if (valueTypeRenderingType == null) {
            return layoutProvider.getLayoutByValueType(valueType);
        } else {
            return layoutProvider.getLayoutByValueRenderingType(valueTypeRenderingType, valueType);
        }
    }
}

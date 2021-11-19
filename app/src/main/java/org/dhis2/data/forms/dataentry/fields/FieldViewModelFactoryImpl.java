package org.dhis2.data.forms.dataentry.fields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import org.dhis2.data.forms.dataentry.fields.age.AgeViewModel;
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel;
import org.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel;
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import org.dhis2.data.forms.dataentry.fields.optionset.OptionSetViewModel;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import org.dhis2.data.forms.dataentry.fields.picture.PictureViewModel;
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel;
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextViewModel;
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedViewModel;
import org.dhis2.data.forms.dataentry.fields.visualOptionSet.MatrixOptionSetModel;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.LegendValue;
import org.dhis2.form.model.RowAction;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import autovalue.shaded.org.checkerframework$.checker.nullness.qual.$NonNull;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

import static org.dhis2.data.forms.dataentry.EnrollmentRepository.SINGLE_SECTION_UID;
import static org.dhis2.utils.Preconditions.isNull;

public final class FieldViewModelFactoryImpl implements FieldViewModelFactory {

    @NonNull
    private final Map<ValueType, String> valueTypeHintMap;

    private final FlowableProcessor<RowAction> fieldProcessor = PublishProcessor.create();
    private final FlowableProcessor<String> sectionProcessor = PublishProcessor.create();
    private final ObservableField<String> currentSection = new ObservableField<String>("");

    private final List<ValueTypeRenderingType> optionSetTextRenderings = Arrays.asList(
            ValueTypeRenderingType.HORIZONTAL_CHECKBOXES,
            ValueTypeRenderingType.VERTICAL_CHECKBOXES,
            ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS,
            ValueTypeRenderingType.VERTICAL_RADIOBUTTONS
    );
    private final boolean searchMode;
    private FormUiColorFactory colorFactory;

    public FieldViewModelFactoryImpl(Map<ValueType, String> valueTypeHintMap, boolean searchMode,
                                     FormUiColorFactory colorFactory) {
        this.valueTypeHintMap = valueTypeHintMap;
        this.searchMode = searchMode;
        this.colorFactory = colorFactory;
    }

    @Nullable
    @Override
    public FieldUiModel createForAttribute(@$NonNull TrackedEntityAttribute trackedEntityAttribute,
                                           @Nullable ProgramTrackedEntityAttribute programTrackedEntityAttribute,
                                           @Nullable String value,
                                           boolean editable) {
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
                fieldProcessor,
                Collections.emptyList(),
                null);
    }

    @NonNull
    @Override
    public FieldUiModel create(@NonNull String id, @NonNull String label, @NonNull ValueType type,
                               @NonNull Boolean mandatory, @Nullable String optionSet, @Nullable String value,
                               @Nullable String section, @Nullable Boolean allowFutureDates, @NonNull Boolean editable, @Nullable ProgramStageSectionRenderingType renderingType,
                               @Nullable String description, @Nullable ValueTypeDeviceRendering fieldRendering, @Nullable Integer optionCount, ObjectStyle objectStyle,
                               @Nullable String fieldMask, @Nullable LegendValue legendValue, @NonNull FlowableProcessor<RowAction> processor, @NonNull List<Option> options, @Nullable String url) {
        isNull(type, "type must be supplied");
        FormUiModelStyle style = new BasicFormUiModelStyle(colorFactory);

        if (searchMode)
            mandatory = false;
        if (DhisTextUtils.Companion.isNotEmpty(optionSet)) {
            if (renderingType == null || renderingType == ProgramStageSectionRenderingType.LISTING) {
                if (fieldRendering != null && (fieldRendering.type().equals(ValueTypeRenderingType.QR_CODE) || fieldRendering.type().equals(ValueTypeRenderingType.BAR_CODE))) {
                    return ScanTextViewModel.create(id, label, mandatory, value, section, editable, optionSet, description, objectStyle, fieldRendering, valueTypeHintMap.get(type), !searchMode, searchMode, processor, url);
                } else if (fieldRendering != null && type == ValueType.TEXT && optionSetTextRenderings.contains(fieldRendering.type())) {
                    return OptionSetViewModel.create(id, label, mandatory, optionSet, value, section, editable, description, objectStyle, true, ProgramStageSectionRenderingType.LISTING.toString(), fieldRendering, processor, options, url);
                } else {
                    return SpinnerViewModel.create(id, label, valueTypeHintMap.get(type), mandatory, optionSet, value, section, editable, description, objectStyle, !searchMode, ProgramStageSectionRenderingType.LISTING.toString(), processor, legendValue, url);
                }
            } else {
                return MatrixOptionSetModel.create(id, label, mandatory, value, section, editable, optionSet, description, objectStyle, processor, url, options, renderingType == ProgramStageSectionRenderingType.MATRIX ? 2 : 1);
            }
        }

        switch (type) {
            case AGE:
                FieldUiModel ageViewModel = AgeViewModel.create(id, label, mandatory, value, section, editable, description, objectStyle, !searchMode, searchMode, processor, style, url);
                return ageViewModel;
            case TEXT:
            case EMAIL:
            case LETTER:
            case NUMBER:
            case INTEGER:
            case LONG_TEXT:
            case PERCENTAGE:
            case PHONE_NUMBER:
            case INTEGER_NEGATIVE:
            case INTEGER_POSITIVE:
            case INTEGER_ZERO_OR_POSITIVE:
            case UNIT_INTERVAL:
            case URL:
                if (fieldRendering != null && (fieldRendering.type().equals(ValueTypeRenderingType.QR_CODE) || fieldRendering.type().equals(ValueTypeRenderingType.BAR_CODE))) {
                    return ScanTextViewModel.create(id, label, mandatory, value, section, editable, optionSet, description, objectStyle, fieldRendering, valueTypeHintMap.get(type), !searchMode, searchMode, processor, url);
                } else {
                    return EditTextViewModel.create(id, label, mandatory, value, valueTypeHintMap.get(type), 1, type, section, editable, description, fieldRendering, objectStyle, fieldMask, ProgramStageSectionRenderingType.LISTING.toString(), !searchMode, searchMode, processor, legendValue, url);
                }
            case IMAGE:
                return PictureViewModel.create(id, label, mandatory, value, section, editable, description, objectStyle, processor, !searchMode, url);
            case TIME:
            case DATE:
            case DATETIME:
                return DateTimeViewModel.create(id, label, mandatory, type, value, section, allowFutureDates, editable, description, objectStyle, !searchMode, searchMode, processor, url);
            case COORDINATE:
                return CoordinateViewModel.create(id, label, mandatory, value, section, editable, description, objectStyle, FeatureType.POINT, !searchMode, searchMode, processor, style, url);
            case BOOLEAN:
            case TRUE_ONLY:
                return RadioButtonViewModel.fromRawValue(id, label, type, mandatory, value, section, editable, description, objectStyle,
                        fieldRendering != null ? fieldRendering.type() : ValueTypeRenderingType.DEFAULT, !searchMode, processor, searchMode, url);
            case ORGANISATION_UNIT:
                return OrgUnitViewModel.create(id, label, mandatory, value, section, editable, description, objectStyle, !searchMode, ProgramStageSectionRenderingType.LISTING.toString(), processor, url);
            case FILE_RESOURCE:
            case TRACKER_ASSOCIATE:
            case USERNAME:
                return UnsupportedViewModel.create(id, label, mandatory, value, section, editable, description, objectStyle, processor, url);
            default:
                return EditTextViewModel.create(id, label, mandatory, value, valueTypeHintMap.get(type), 1, type, section, editable, description, fieldRendering, objectStyle, fieldMask, ProgramStageSectionRenderingType.LISTING.toString(), !searchMode, searchMode, processor, legendValue, url);
        }
    }

    @NonNull
    @Override
    public FieldUiModel createSingleSection(String singleSectionName) {
        return SectionViewModel.create(
                SINGLE_SECTION_UID,
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

    @Override
    public BasicFormUiModelStyle style(){
        return new BasicFormUiModelStyle(colorFactory);
    }
}

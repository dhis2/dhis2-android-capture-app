package com.dhis2.data.forms.dataentry.fields;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.data.forms.dataentry.fields.age.AgeViewModel;
import com.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel;
import com.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel;
import com.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import com.dhis2.data.forms.dataentry.fields.file.FileViewModel;
import com.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import com.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel;
import com.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;

import org.hisp.dhis.android.core.common.ValueType;

import static android.text.TextUtils.isEmpty;
import static com.dhis2.utils.Preconditions.isNull;

public final class FieldViewModelFactoryImpl implements FieldViewModelFactory {

    @NonNull
    private final String hintEnterText;

    @NonNull
    private final String hintEnterLongText;

    @NonNull
    private final String hintEnterNumber;

    @NonNull
    private final String hintEnterInteger;

    @NonNull
    private final String hintEnterIntegerPositive;

    @NonNull
    private final String hintEnterIntegerNegative;

    @NonNull
    private final String hintEnterIntegerZeroOrPositive;

    @NonNull
    private final String hintFilterOptions;

    @NonNull
    private final String hintChooseDate;

    public FieldViewModelFactoryImpl(@NonNull String hintEnterText, @NonNull String hintEnterLongText,
                                     @NonNull String hintEnterNumber, @NonNull String hintEnterInteger,
                                     @NonNull String hintEnterIntegerPositive, @NonNull String hintEnterIntegerNegative,
                                     @NonNull String hintEnterIntegerZeroOrPositive, @NonNull String filterOptions,
                                     @NonNull String hintChooseDate) {
        this.hintEnterText = hintEnterText;
        this.hintEnterLongText = hintEnterLongText;
        this.hintEnterNumber = hintEnterNumber;
        this.hintEnterInteger = hintEnterInteger;
        this.hintEnterIntegerPositive = hintEnterIntegerPositive;
        this.hintEnterIntegerNegative = hintEnterIntegerNegative;
        this.hintEnterIntegerZeroOrPositive = hintEnterIntegerZeroOrPositive;
        this.hintFilterOptions = filterOptions;
        this.hintChooseDate = hintChooseDate;
    }

    @NonNull
    @Override
    @SuppressWarnings({
            "PMD.CyclomaticComplexity",
            "PMD.StdCyclomaticComplexity"
    })
    public FieldViewModel create(@NonNull String id, @NonNull String label, @NonNull ValueType type,
                                 @NonNull Boolean mandatory, @Nullable String optionSet, @Nullable String value,@Nullable String section, @Nullable Boolean allowFutureDates, @NonNull Boolean editable) {
        isNull(type, "type must be supplied");

        if (!isEmpty(optionSet)) {
            return SpinnerViewModel.create(id, label, hintFilterOptions, mandatory, optionSet, value, section);
        }

        switch (type) {
            case AGE:
                return AgeViewModel.create(id, label, mandatory, value, section);
            case TEXT:
            case EMAIL:
            case LETTER:
            case NUMBER:
            case INTEGER:
            case USERNAME:
            case LONG_TEXT:
            case PERCENTAGE:
            case PHONE_NUMBER:
            case INTEGER_NEGATIVE:
            case INTEGER_POSITIVE:
            case INTEGER_ZERO_OR_POSITIVE:
            case UNIT_INTERVAL:
                return EditTextViewModel.create(id, label, mandatory, value, hintEnterText, 1, type, section,editable);
            case TIME:
            case DATE:
            case DATETIME:
                return DateTimeViewModel.create(id, label, mandatory, type, value, section, allowFutureDates);
            case FILE_RESOURCE:
                return FileViewModel.create(id, label, mandatory, value, section);
            case COORDINATE:
                return CoordinateViewModel.create(id, label, mandatory, value, section);
            case BOOLEAN:
            case TRUE_ONLY:
                return RadioButtonViewModel.fromRawValue(id, label, type, mandatory, value, section);
            case ORGANISATION_UNIT:
                return OrgUnitViewModel.create(id, label, mandatory, value, section);
            case TRACKER_ASSOCIATE:
            case URL:
            default:
                return EditTextViewModel.create(id, label, mandatory, value, hintEnterText, 1, type, section,editable);
        }
    }

   /* @NonNull
    private DateViewModel createDate(@NonNull String uid, @NonNull String label,
                                     @NonNull Boolean mandatory, @Nullable String value) {
        return DateViewModel.forDate(uid, label, hintChooseDate, mandatory, value);
    }

    @NonNull
    private DateViewModel createDateTime(@NonNull String uid, @NonNull String label,
                                         @NonNull Boolean mandatory, @Nullable String value) {
        return DateViewModel.forDateTime(uid, label, hintChooseDate, mandatory, value);
    }

    @NonNull
    private OptionsViewModel createOption(@NonNull String id, @NonNull String label,
                                          @NonNull Boolean mandatory, @NonNull String optionSet, @Nullable String value) {
        return OptionsViewModel.create(id, label, hintFilterOptions, mandatory, optionSet, value);
    }*/

    /*@NonNull
    private EditTextViewModel createText(@NonNull String id, @NonNull String label,
                                         @NonNull Boolean mandatory, @Nullable String value) {
        return EditTextViewModel.create(id, label, mandatory, value, hintEnterText, 1);
    }

    @NonNull
    private EditTextViewModel createLongText(@NonNull String id, @NonNull String label,
                                             @NonNull Boolean mandatory, @Nullable String value) {
        return EditTextViewModel.create(id, label, mandatory, value, hintEnterLongText, 3);
    }*/

    /*@NonNull
    private EditTextDoubleViewModel createNumber(@NonNull String id, @NonNull String label,
                                                 @NonNull Boolean mandatory, @Nullable String value) {
        return EditTextDoubleViewModel.fromRawValue(id, label, mandatory, value, hintEnterNumber);
    }

    @NonNull
    private EditTextIntegerViewModel createInteger(@NonNull String id, @NonNull String label,
                                                   @NonNull Boolean mandatory, @Nullable String value) {
        return EditTextIntegerViewModel.fromRawValue(id, label, mandatory, value, hintEnterInteger,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
    }

    @NonNull
    private EditTextIntegerViewModel createIntegerPositive(@NonNull String id, @NonNull String label,
                                                           @NonNull Boolean mandatory, @Nullable String value) {
        return EditTextIntegerViewModel.fromRawValue(id, label, mandatory, value,
                hintEnterIntegerPositive, InputType.TYPE_CLASS_NUMBER);
    }

    @NonNull
    private EditTextIntegerViewModel createIntegerNegative(@NonNull String id, @NonNull String label,
                                                           @NonNull Boolean mandatory, @Nullable String value) {
        return EditTextIntegerViewModel.fromRawValue(id, label, mandatory, value, hintEnterIntegerNegative,
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
    }

    @NonNull
    private EditTextIntegerViewModel createIntegerZeroOrPositive(@NonNull String id, @NonNull String label,
                                                                 @NonNull Boolean mandatory, @Nullable String value) {
        return EditTextIntegerViewModel.fromRawValue(id, label, mandatory, value,
                hintEnterIntegerZeroOrPositive, InputType.TYPE_CLASS_NUMBER);
    }*/
}

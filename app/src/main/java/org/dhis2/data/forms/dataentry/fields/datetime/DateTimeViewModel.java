package org.dhis2.data.forms.dataentry.fields.datetime;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.RowAction;
import org.dhis2.form.ui.intent.FormIntent;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;

import java.util.Date;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class DateTimeViewModel extends FieldViewModel {

    public abstract boolean isBackgroundTransparent();

    @NonNull
    public abstract ValueType valueType();

    public static FieldViewModel create(String id, int layoutId, String label, Boolean mandatory, ValueType type, String value, String section, Boolean allowFutureDates, Boolean editable, String description, ObjectStyle objectStyle, boolean isBackgroundTransparent, boolean isSearchMode, FlowableProcessor<RowAction> processor) {
        return new AutoValue_DateTimeViewModel(id, layoutId, label, mandatory, value, section, allowFutureDates, editable, null, null, null, description, objectStyle, null, provideDataEntryViewHolderType(type), processor, null,false, isBackgroundTransparent, type, isSearchMode);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_DateTimeViewModel(uid(), layoutId(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, provideDataEntryViewHolderType(valueType()), processor(), style(),activated(), isBackgroundTransparent(), valueType(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_DateTimeViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), null, provideDataEntryViewHolderType(valueType()), processor(), style(),activated(), isBackgroundTransparent(), valueType(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_DateTimeViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), null, provideDataEntryViewHolderType(valueType()), processor(), style(),activated(), isBackgroundTransparent(), valueType(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_DateTimeViewModel(uid(), layoutId(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, provideDataEntryViewHolderType(valueType()), processor(), style(),activated(), isBackgroundTransparent(), valueType(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_DateTimeViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), isEditable, optionSet(), warning(), error(), description(), objectStyle(), null, provideDataEntryViewHolderType(valueType()), processor(), style(),activated(), isBackgroundTransparent(), valueType(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_DateTimeViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, provideDataEntryViewHolderType(valueType()), processor(), style(),isFocused, isBackgroundTransparent(), valueType(), isSearchMode());
    }

    private static DataEntryViewHolderTypes provideDataEntryViewHolderType(ValueType type) {
        switch (type) {
            case DATE:
                return DataEntryViewHolderTypes.DATE;
            case TIME:
                return DataEntryViewHolderTypes.TIME;
            case DATETIME:
            default:
                return DataEntryViewHolderTypes.DATETIME;
        }
    }

    public void onDateSelected(Date date) {
        String dateFormatted = "";
        if (date != null) {
            switch (valueType()) {
                case DATETIME:
                    dateFormatted = DateUtils.databaseDateFormat().format(date);
                    break;
                case DATE:
                    dateFormatted = DateUtils.oldUiDateFormat().format(date);
                    break;
                case TIME:
                default:
                    dateFormatted = DateUtils.timeFormat().format(date);
            }
        }
        callback.intent(new FormIntent.OnSave(
                uid(),
                date != null ? dateFormatted : null,
                valueType(),
                fieldMask()
        ));
    }

    public abstract boolean isSearchMode();
}

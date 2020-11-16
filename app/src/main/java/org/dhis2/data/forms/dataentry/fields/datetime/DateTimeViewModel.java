package org.dhis2.data.forms.dataentry.fields.datetime;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;

import java.util.Date;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class DateTimeViewModel extends FieldViewModel {

    public boolean activated = false;

    public abstract boolean isBackgroundTransparent();

    @NonNull
    public abstract ValueType valueType();

    public static FieldViewModel create(String id, String label, Boolean mandatory, ValueType type, String value, String section, Boolean allowFutureDates, Boolean editable, String description, ObjectStyle objectStyle, boolean isBackgroundTransparent, boolean isSearchMode) {
        return new AutoValue_DateTimeViewModel(id, label, mandatory, value, section, allowFutureDates, editable, null, null, null, description, objectStyle, null, provideDataEntryViewHolderType(type), isBackgroundTransparent, type, isSearchMode);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_DateTimeViewModel(uid(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, provideDataEntryViewHolderType(valueType()), isBackgroundTransparent(), valueType(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_DateTimeViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), null, provideDataEntryViewHolderType(valueType()), isBackgroundTransparent(), valueType(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_DateTimeViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), null, provideDataEntryViewHolderType(valueType()), isBackgroundTransparent(), valueType(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_DateTimeViewModel(uid(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, provideDataEntryViewHolderType(valueType()), isBackgroundTransparent(), valueType(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_DateTimeViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), isEditable, optionSet(), warning(), error(), description(), objectStyle(), null, provideDataEntryViewHolderType(valueType()), isBackgroundTransparent(), valueType(), isSearchMode());
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

    @Override
    public int getLayoutId() {
        switch (valueType()) {
            case DATE:
                return R.layout.form_date_text;
            case TIME:
                return R.layout.form_time_text;
            case DATETIME:
            default:
                return R.layout.form_date_time_text;
        }
    }

    public void onDateSelected(Date date) {
        /*
            DATETIME
            String dateFormatted = dateFormatted = DateUtils.databaseDateFormat().format(date);

            DATE
            String dateFormatted = DateUtils.oldUiDateFormat().format(date);

            TIME
            String dateFormatted = DateUtils.timeFormat().format(date);

            RowAction rowAction = RowAction.create(dateTimeViewModel.uid(), date != null ? dateFormatted : null, getAdapterPosition());
            if (processor != null) {
                processor.onNext(rowAction);
            }


       */
    }

    public void onActivate() {
        activated = true;
    }

    public void onDeactivate() {
        activated = false;
    }

    public abstract boolean isSearchMode();
}

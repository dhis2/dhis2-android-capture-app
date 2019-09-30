package org.dhis2.data.forms.dataentry.fields.datetime;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;

import javax.annotation.Nonnull;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class DateTimeViewModel extends FieldViewModel {

    @NonNull
    public abstract ValueType valueType();

    public static FieldViewModel create(String id, String label, Boolean mandatory, ValueType type, String value, String section, Boolean allowFutureDates, Boolean editable, String description, ObjectStyle objectStyle) {
        return new AutoValue_DateTimeViewModel(id, label, mandatory, value, section, allowFutureDates, editable, null, null, null, description, objectStyle, null, type);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_DateTimeViewModel(uid(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, valueType());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_DateTimeViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), null, valueType());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_DateTimeViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), null, valueType());
    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_DateTimeViewModel(uid(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, valueType());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_DateTimeViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), isEditable, optionSet(), warning(), error(), description(), objectStyle(), null, valueType());
    }
}

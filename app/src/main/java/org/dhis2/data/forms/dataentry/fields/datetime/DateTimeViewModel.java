package org.dhis2.data.forms.dataentry.fields.datetime;

import android.support.annotation.NonNull;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.common.ValueType;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class DateTimeViewModel extends FieldViewModel {

    @NonNull
    public abstract ValueType valueType();

    public static FieldViewModel create(String id, String label, Boolean mandatory, ValueType type, String value, String section, Boolean allowFutureDates, Boolean editable,String description) {
        return new AutoValue_DateTimeViewModel(id, label, mandatory, value,section, allowFutureDates,editable,null,null,null,description, type);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_DateTimeViewModel(uid(),label(),true,value(),programStageSection(),
                allowFutureDate(),editable(),optionSet(),warning(),error(),description(),valueType());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_DateTimeViewModel(uid(),label(),mandatory(),value(),programStageSection(),
                allowFutureDate(),editable(),optionSet(),warning(),error,description(),valueType());    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_DateTimeViewModel(uid(),label(),mandatory(),value(),programStageSection(),
                allowFutureDate(),editable(),optionSet(),warning,error(),description(),valueType());    }
}

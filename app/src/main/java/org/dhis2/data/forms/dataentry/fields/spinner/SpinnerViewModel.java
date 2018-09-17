package org.dhis2.data.forms.dataentry.fields.spinner;

import android.support.annotation.NonNull;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.google.auto.value.AutoValue;

/**
 * Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class SpinnerViewModel extends FieldViewModel {

    @NonNull
    public abstract String hint();

    @NonNull
    public abstract String optionSet();

    public static SpinnerViewModel create(String id, String label, String hintFilterOptions, Boolean mandatory, String optionSet, String value, String section, Boolean editable) {
        return new AutoValue_SpinnerViewModel(id, label, mandatory, value, section, null, editable,null,null, hintFilterOptions, optionSet);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_SpinnerViewModel(uid(),label(),true,value(),programStageSection(),allowFutureDate(),editable(),warning(),error(),hint(),optionSet());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_SpinnerViewModel(uid(),label(),mandatory(),value(),programStageSection(),allowFutureDate(),editable(),warning(),error,hint(),optionSet());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_SpinnerViewModel(uid(),label(),mandatory(),value(),programStageSection(),allowFutureDate(),editable(),warning,error(),hint(),optionSet());
    }
}

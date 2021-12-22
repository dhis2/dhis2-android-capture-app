package org.dhis2.data.forms.dataentry.fields.unsupported;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;

@AutoValue
public abstract class UnsupportedViewModel extends FieldViewModel {

    public static FieldViewModel create(String id, int layoutId, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, ValueType valueType, String url) {
        return new AutoValue_UnsupportedViewModel(id, layoutId, label, false, value, section, null, false, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.UNSUPPORTED, null, null, false, valueType, url);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_UnsupportedViewModel(uid(), layoutId(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, style(), hint(), activated(), valueType(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_UnsupportedViewModel(uid(), layoutId(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, style(), hint(), activated(), valueType(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_UnsupportedViewModel(uid(), layoutId(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, style(), hint(), activated(), valueType(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_UnsupportedViewModel(uid(), layoutId(), label(), false, data, programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, style(), hint(), activated(), valueType(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_UnsupportedViewModel(uid(), layoutId(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, style(), hint(), activated(), valueType(), url());
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_UnsupportedViewModel(uid(), layoutId(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, style(), hint(), isFocused, valueType(), url());
    }
}

package org.dhis2.data.forms.dataentry.fields.file;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;

@AutoValue
public abstract class FileViewModel extends FieldViewModel {

    public static FieldViewModel create(String id, int layoutId, String label, Boolean mandatory, String value, String section, String description, ObjectStyle objectStyle) {
        return new AutoValue_FileViewModel(id, layoutId, label, mandatory, value, section, null,
                true, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.BUTTON, null, null, false, ValueType.FILE_RESOURCE, url);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_FileViewModel(uid(), layoutId(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON, style(), hint(), activated(), valueType(), url);
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_FileViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON, style(), hint(), activated(), valueType(), url);
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_FileViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON, style(), hint(), activated(), valueType(), url);
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_FileViewModel(uid(), layoutId(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON, style(), hint(), activated(), valueType(), url);
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_FileViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), isEditable, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON, style(), hint(), activated(), valueType(), url);
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_FileViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON, style(), hint(), isFocused, valueType(), url);
    }
}

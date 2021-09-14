package org.dhis2.data.forms.dataentry.fields.file;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.RowAction;
import org.hisp.dhis.android.core.common.ObjectStyle;

import io.reactivex.processors.FlowableProcessor;

@AutoValue
public abstract class FileViewModel extends FieldViewModel {

    public static FieldViewModel create(String id, int layoutId, String label, Boolean mandatory, String value, String section, String description, ObjectStyle objectStyle, FlowableProcessor<RowAction> processor) {
        return new AutoValue_FileViewModel(id, layoutId, label, mandatory, value, section, null,
                true, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.BUTTON, processor,null, false);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_FileViewModel(uid(), layoutId(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON, processor(), style(),activated());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_FileViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON, processor(), style(),activated());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_FileViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON, processor(), style(),activated());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_FileViewModel(uid(), layoutId(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON, processor(), style(),activated());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_FileViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), isEditable, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON, processor(), style(),activated());
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_FileViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.BUTTON, processor(), style(),isFocused);
    }
}

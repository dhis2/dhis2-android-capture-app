package org.dhis2.data.forms.dataentry.fields.age;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;

/**
 * QUADRAM. Created by frodriguez on 20/03/2018.
 */

@AutoValue
public abstract class AgeViewModel extends FieldViewModel {

    @NonNull
    public abstract Boolean mandatory();

    @Nullable
    public abstract String value();

    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, boolean isBackgroundTransparent) {
        return new AutoValue_AgeViewModel(id, label, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.AGE_VIEW, mandatory, value, isBackgroundTransparent);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.AGE_VIEW, true, value(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.AGE_VIEW, mandatory(), value(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.AGE_VIEW, mandatory(), value(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.AGE_VIEW, mandatory(), data, isBackgroundTransparent());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), isEditable, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.AGE_VIEW, mandatory(), value(), isBackgroundTransparent());
    }

    @Override
    public int getLayoutId() {
        return R.layout.form_age_custom;
    }

    public abstract Boolean isBackgroundTransparent();
}

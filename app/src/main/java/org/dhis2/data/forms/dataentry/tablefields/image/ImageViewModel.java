package org.dhis2.data.forms.dataentry.tablefields.image;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;

import javax.annotation.Nonnull;

/**
 * QUADRAM. Created by ppajuelo on 31/05/2018.
 */
@AutoValue
public abstract class ImageViewModel extends FieldViewModel {

    public static ImageViewModel create(String id, String label, String optionSet, String value, String section, Boolean editable, Boolean mandatory, String description) {
        return new AutoValue_ImageViewModel(id, label, mandatory, value, section, true, editable, optionSet, null, null, description);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_ImageViewModel(uid(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(),description());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_ImageViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error,description());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_ImageViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error(),description());
    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_ImageViewModel(uid(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(),description());    }
}

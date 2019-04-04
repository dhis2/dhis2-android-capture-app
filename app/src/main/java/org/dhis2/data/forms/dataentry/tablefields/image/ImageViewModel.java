package org.dhis2.data.forms.dataentry.tablefields.image;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * QUADRAM. Created by ppajuelo on 31/05/2018.
 */
@AutoValue
public abstract class ImageViewModel extends FieldViewModel {

    public static ImageViewModel create(String id, String label, String optionSet, String value, String section, Boolean editable, Boolean mandatory, String description, String dataElement, List<String> listCategoryOption, String storeBy, int row, int column, String categoryOptionCombo, String catCombo) {
        return new AutoValue_ImageViewModel(id, label, mandatory, value, section, true, editable, optionSet, null, null, description, dataElement, listCategoryOption, storeBy, row, column, categoryOptionCombo, catCombo);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_ImageViewModel(uid(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(),description(),dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(), catCombo());
    }

    @Override
    public FieldViewModel setValue(String value) {
        return new AutoValue_ImageViewModel(uid(), label(), mandatory(), value, programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(),description(),dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(), catCombo());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_ImageViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error,description(),dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(), catCombo());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_ImageViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error(),description(),dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(), catCombo());
    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_ImageViewModel(uid(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(),description(),dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(), catCombo());    }
}

package org.dhis2.data.forms.dataentry.tablefields.coordinate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class CoordinateViewModel extends FieldViewModel {
    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, @NonNull String dataElement, @NonNull List<String> listCategoryOption, @NonNull String storeBy, @Nullable int row, @Nullable int column, String categoryOptionCombo, String catCombo) {
        return new AutoValue_CoordinateViewModel(id, label, mandatory, value, section, null, editable, null, null, null,description,dataElement, listCategoryOption, storeBy, row, column, categoryOptionCombo, catCombo);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_CoordinateViewModel(uid(), label(), true, value(), programStageSection(), null, editable(), null, warning(), error(),description(), dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(),catCombo());
    }

    @Override
    public FieldViewModel setValue(String value) {
        return new AutoValue_CoordinateViewModel(uid(), label(), mandatory(), value, programStageSection(), null, editable(), null, warning(), error(),description(), dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(),catCombo());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_CoordinateViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning, error(),description(), dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(), catCombo());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_CoordinateViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning(), error,description(), dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(), catCombo());
    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_CoordinateViewModel(uid(), label(), mandatory(), data, programStageSection(), null, editable(), null, warning(), error(),description(), dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(), catCombo());
    }
}

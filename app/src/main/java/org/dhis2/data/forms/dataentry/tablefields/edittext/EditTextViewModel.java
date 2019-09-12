package org.dhis2.data.forms.dataentry.tablefields.edittext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.InputType;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.hisp.dhis.android.core.common.ValueType;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class EditTextViewModel extends EditTextModel<String> {

    @NonNull
    public static EditTextViewModel create(@NonNull String uid, @NonNull String label,
                                           @NonNull Boolean mandatory, @Nullable String value, @NonNull String hint,
                                           @NonNull Integer lines, @NonNull ValueType valueType, @Nullable String section, @NonNull Boolean editable, @Nullable String description,
                                           @Nullable String dataElement, @Nullable List<String> listCategoryOption, @Nullable String storeBy, @Nullable int row, @Nullable int column, @Nullable String categoryOptionCombo, @Nullable String catCombo) {
        return new AutoValue_EditTextViewModel(uid, label, mandatory,
                value, section, null, editable, null, description, dataElement, listCategoryOption, storeBy, row, column, categoryOptionCombo, catCombo, hint, lines, InputType.TYPE_CLASS_TEXT, valueType, null, null);
    }

    @NonNull
    @Override
    public EditTextViewModel withWarning(@NonNull String warning) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), null, true, null, description(), dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(),catCombo(),hint(), maxLines(), inputType(), valueType(), warning, error());
    }

    @Override
    public FieldViewModel setValue(String value) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value, programStageSection(), null, true, null, description(), dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(),catCombo(),hint(), maxLines(), inputType(), valueType(), warning(), error());
    }

    @NonNull
    @Override
    public EditTextViewModel withError(@NonNull String error) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                value(), programStageSection(), null, true, null, description(), dataElement(),listCategoryOption(), storeBy(),row(), column(), categoryOptionCombo(),catCombo(),hint(), maxLines(), inputType(), valueType(), warning(), error);
    }

    @NonNull
    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_EditTextViewModel(uid(), label(), true,
                value(), programStageSection(), null, editable(), null, description(), dataElement(),listCategoryOption(), storeBy(),row(), column(), categoryOptionCombo(),catCombo(),hint(), maxLines(), InputType.TYPE_CLASS_TEXT, valueType(), warning(), error());
    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_EditTextViewModel(uid(), label(), mandatory(),
                data, programStageSection(), null, editable(), null, description(), dataElement(),listCategoryOption(), storeBy(),row(), column(), categoryOptionCombo(),catCombo(), hint(), maxLines(), InputType.TYPE_CLASS_TEXT, valueType(), warning(), error());
    }
}

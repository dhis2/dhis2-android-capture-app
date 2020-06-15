package org.dhis2.data.forms.dataentry.tablefields.datetime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.hisp.dhis.android.core.common.ValueType;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class DateTimeViewModel extends FieldViewModel {

    @NonNull
    public abstract ValueType valueType();

    public static FieldViewModel create(String id, String label, Boolean mandatory, ValueType type, String value, String section, Boolean allowFutureDates, Boolean editable, String description,
                                        @Nullable String dataElement, @Nullable List<String> listCategoryOption, @Nullable String storeBy, @Nullable int row, @Nullable int column, String categoryOptionCombo, String catCombo) {
        return new AutoValue_DateTimeViewModel(id, label, mandatory, value,section, allowFutureDates,editable,null,null,null,description, dataElement, listCategoryOption, storeBy, row, column, categoryOptionCombo,catCombo, type);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_DateTimeViewModel(uid(),label(),true,value(),programStageSection(),
                allowFutureDate(),editable(),optionSet(),warning(),error(),description(), dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(),catCombo(), valueType());
    }

    @Override
    public FieldViewModel setValue(String value) {
        return new AutoValue_DateTimeViewModel(uid(),label(),mandatory(),value,programStageSection(),
                allowFutureDate(),editable(),optionSet(),warning(),error(),description(), dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(),catCombo(), valueType());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_DateTimeViewModel(uid(),label(),mandatory(),value(),programStageSection(),
                allowFutureDate(),editable(),optionSet(),warning(),error,description(), dataElement(),listCategoryOption(), storeBy(), row(), column(), categoryOptionCombo(),catCombo(), valueType());    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_DateTimeViewModel(uid(),label(),mandatory(),value(),programStageSection(),
                allowFutureDate(),editable(),optionSet(),warning,error(),description(), dataElement(),listCategoryOption(), storeBy(),row(), column(), categoryOptionCombo(),catCombo(), valueType());    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_DateTimeViewModel(uid(),label(),mandatory(),data,programStageSection(),
                allowFutureDate(),editable(),optionSet(),warning(),error(),description(), dataElement(),listCategoryOption(), storeBy(),row(), column(), categoryOptionCombo(),catCombo(), valueType());    }
}

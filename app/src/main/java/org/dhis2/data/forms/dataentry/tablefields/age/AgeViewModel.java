package org.dhis2.data.forms.dataentry.tablefields.age;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.composetable.model.DropdownOption;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;

import java.util.List;


/**
 * QUADRAM. Created by frodriguez on 20/03/2018.
 */

@AutoValue
public abstract class AgeViewModel extends  FieldViewModel {

    @NonNull
    public abstract Boolean mandatory();

    @Nullable
    public abstract String value();

    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, @NonNull String dataElement, @NonNull List<String> listCategoryOption, @NonNull String storeBy, @Nullable int row, @Nullable int column, @Nullable String categoryOptionCombo, String catCombo, @Nullable List<DropdownOption> options) {
        return new AutoValue_AgeViewModel(id, label, section, null, editable, null, null, null, description,dataElement, listCategoryOption, options, storeBy , row, column, categoryOptionCombo, catCombo, mandatory, value);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), dataElement(), options(), optionsList(), storeBy(), row(), column(), categoryOptionCombo(), catCombo(), true, value());
    }

    @Override
    public FieldViewModel setValue(String value) {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), dataElement(), options(), optionsList(), storeBy(), row(), column(), categoryOptionCombo(), catCombo(), mandatory(), value);
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error, description(), dataElement(), options(), optionsList(), storeBy(),row(), column(), categoryOptionCombo(),catCombo(), mandatory(), value());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning, error(), description(), dataElement(), options(), optionsList(), storeBy(),row(), column(), categoryOptionCombo(),catCombo(), mandatory(), value());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), dataElement(), options(), optionsList(), storeBy(),row(), column(), categoryOptionCombo(),catCombo(), mandatory(), data);
    }
}

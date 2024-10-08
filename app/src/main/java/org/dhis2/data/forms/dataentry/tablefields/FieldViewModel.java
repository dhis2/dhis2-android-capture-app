package org.dhis2.data.forms.dataentry.tablefields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.composetable.model.DropdownOption;

import java.util.List;

public abstract class FieldViewModel {

    @NonNull
    public abstract String uid();

    @NonNull
    public abstract String label();

    @NonNull
    public abstract Boolean mandatory();

    @Nullable
    public abstract String value();

    @Nullable
    public abstract String programStageSection();

    @Nullable
    public abstract Boolean allowFutureDate();

    @Nullable
    public abstract Boolean editable();

    @Nullable
    public abstract String optionSet();

    @Nullable
    public abstract String warning();

    @Nullable
    public abstract String error();

    public abstract FieldViewModel setMandatory();

    public abstract FieldViewModel setValue(String value);

    @NonNull
    public abstract FieldViewModel withWarning(@NonNull String warning);

    @NonNull
    public abstract FieldViewModel withError(@NonNull String error);

    @Nullable
    public abstract String description();

   @NonNull
    public abstract FieldViewModel withValue(String data);

   @NonNull
    public abstract String dataElement();

   @NonNull
    public abstract List<String> options();

   @NonNull
    public abstract List<DropdownOption> optionsList();

   @NonNull
    public abstract String storeBy();

    public abstract int row();

    public abstract int column();

    public abstract String categoryOptionCombo();

    public abstract String catCombo();
}
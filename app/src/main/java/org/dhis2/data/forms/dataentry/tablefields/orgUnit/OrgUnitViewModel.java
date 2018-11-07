package org.dhis2.data.forms.dataentry.tablefields.orgUnit;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;

import javax.annotation.Nonnull;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */
@AutoValue
public abstract class OrgUnitViewModel extends FieldViewModel {

    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description) {
        return new AutoValue_OrgUnitViewModel(id, label, mandatory, value,section, null,editable,null,null,null,description);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_OrgUnitViewModel(uid(),label(),true,value(),programStageSection(),allowFutureDate(),editable(),optionSet(),warning(),error(),description());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_OrgUnitViewModel(uid(),label(),mandatory(),value(),programStageSection(),allowFutureDate(),editable(),optionSet(),warning(),error,description());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_OrgUnitViewModel(uid(),label(),mandatory(),value(),programStageSection(),allowFutureDate(),editable(),optionSet(),warning,error(),description());
    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_OrgUnitViewModel(uid(),label(),mandatory(),data,programStageSection(),allowFutureDate(),editable(),optionSet(),warning(),error(),description());
    }
}

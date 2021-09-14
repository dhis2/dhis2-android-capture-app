package org.dhis2.data.forms.dataentry.fields.orgUnit;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.RowAction;
import org.dhis2.form.ui.intent.FormIntent;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */
@AutoValue
public abstract class OrgUnitViewModel extends FieldViewModel {

    public abstract boolean isBackgroundTransparent();

    public abstract String renderType();

    public static FieldViewModel create(String id, int layoutId, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, boolean isBackgroundTransparent, String renderType, FlowableProcessor<RowAction> processor) {
        return new AutoValue_OrgUnitViewModel(id, layoutId, label, mandatory, value, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.ORG_UNIT, processor, null, false, isBackgroundTransparent, renderType);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_OrgUnitViewModel(uid(), layoutId(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.ORG_UNIT, processor(), style(), activated(), isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_OrgUnitViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.ORG_UNIT, processor(), style(), activated(), isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_OrgUnitViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.ORG_UNIT, processor(), style(), activated(), isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_OrgUnitViewModel(uid(), layoutId(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.ORG_UNIT, processor(), style(), activated(), isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_OrgUnitViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), isEditable, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.ORG_UNIT, processor(), style(), activated(), isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_OrgUnitViewModel(uid(), layoutId(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.ORG_UNIT, processor(), style(), isFocused, isBackgroundTransparent(), renderType());
    }

    public void onDataChange(String orgUnitUid, String orgUnitName) {
        callback.intent(new FormIntent.OnSave(
                uid(),
                isBackgroundTransparent() ? orgUnitUid : orgUnitUid != null ? orgUnitUid + "_ou_" + orgUnitName : null,
                ValueType.ORGANISATION_UNIT,
                fieldMask()
        ));
    }
}

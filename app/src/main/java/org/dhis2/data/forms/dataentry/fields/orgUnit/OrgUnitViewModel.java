package org.dhis2.data.forms.dataentry.fields.orgUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.ui.intent.FormIntent;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;

/**
 * QUADRAM. Created by ppajuelo on 19/03/2018.
 */
@AutoValue
public abstract class OrgUnitViewModel extends FieldViewModel {

    public abstract boolean isBackgroundTransparent();

    public abstract String renderType();

    @Nullable
    public abstract String displayName();

    public static FieldViewModel create(
            String id,
            int layoutId,
            String label,
            Boolean mandatory,
            String value,
            String section,
            Boolean editable,
            String description,
            ObjectStyle objectStyle,
            boolean isBackgroundTransparent,
            String renderType,
            String displayName,
            String url
    ) {
        return new AutoValue_OrgUnitViewModel(
                id,
                layoutId,
                label,
                mandatory,
                value,
                section,
                null,
                editable,
                null,
                null,
                null,
                description,
                objectStyle,
                null,
                DataEntryViewHolderTypes.ORG_UNIT,
                null,
                null,
                false,
                ValueType.ORGANISATION_UNIT, url
                isBackgroundTransparent,
                renderType,
                displayName
        );
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_OrgUnitViewModel(
                uid(),
                layoutId(),
                label(),
                true,
                value(),
                programStageSection(),
                allowFutureDate(),
                editable(),
                optionSet(),
                warning(),
                error(),
                description(),
                objectStyle(),
                null,
                DataEntryViewHolderTypes.ORG_UNIT,
                style(),
                hint(),
                activated(),
                valueType(),
                url
                isBackgroundTransparent(),
                renderType(),
                displayName()
        );
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_OrgUnitViewModel(
                uid(),
                layoutId(),
                label(),
                mandatory(),
                value(),
                programStageSection(),
                allowFutureDate(),
                editable(),
                optionSet(),
                warning(),
                error,
                description(),
                objectStyle(),
                null,
                DataEntryViewHolderTypes.ORG_UNIT,
                style(),
                hint(),
                activated(),
                valueType(),
                url
                isBackgroundTransparent(),
                renderType(),
                displayName()
        );
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_OrgUnitViewModel(
                uid(),
                layoutId(),
                label(),
                mandatory(),
                value(),
                programStageSection(),
                allowFutureDate(),
                editable(),
                optionSet(),
                warning,
                error(),
                description(),
                objectStyle(),
                null,
                DataEntryViewHolderTypes.ORG_UNIT,
                style(),
                hint(),
                activated(),
                valueType(),
                url
                isBackgroundTransparent(),
                renderType(),
                displayName()
        );
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_OrgUnitViewModel(
                uid(),
                layoutId(),
                label(),
                mandatory(),
                data,
                programStageSection(),
                allowFutureDate(),
                editable(),
                optionSet(),
                warning(),
                error(),
                description(),
                objectStyle(),
                null,
                DataEntryViewHolderTypes.ORG_UNIT,
                style(),
                hint(),
                activated(),
                valueType(),
                url
                isBackgroundTransparent(),
                renderType(),
                displayName()
        );
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_OrgUnitViewModel(
                uid(),
                layoutId(),
                label(),
                mandatory(),
                value(),
                programStageSection(),
                allowFutureDate(),
                isEditable,
                optionSet(),
                warning(),
                error(),
                description(),
                objectStyle(),
                null,
                DataEntryViewHolderTypes.ORG_UNIT,
                style(),
                hint(),
                activated(),
                valueType(),
                url
                isBackgroundTransparent(),
                renderType(),
                displayName()
        );
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_OrgUnitViewModel(
                uid(),
                layoutId(),
                label(),
                mandatory(),
                value(),
                programStageSection(),
                allowFutureDate(),
                editable(),
                optionSet(),
                warning(),
                error(),
                description(),
                objectStyle(),
                null,
                DataEntryViewHolderTypes.ORG_UNIT,
                style(),
                hint(),
                isFocused,
                valueType(),
                url
                isBackgroundTransparent(),
                renderType(),
                displayName()
        );
    }

    @NonNull
    @Override
    public FieldUiModel withDisplayName(String displayName) {
        return new AutoValue_OrgUnitViewModel(
                uid(),
                layoutId(),
                label(), mandatory(),
                value(),
                programStageSection(),
                allowFutureDate(),
                editable(), optionSet(),
                warning(),
                error(),
                description(),
                objectStyle(),
                fieldMask(),
                DataEntryViewHolderTypes.ORG_UNIT,
                style(),
                hint(),
                getFocused(),
                valueType(),
                isBackgroundTransparent(),
                renderType(),
                displayName
        );
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return displayName();
    }

    public void onDataChange(String orgUnitUid, String orgUnitName) {
        callback.intent(new FormIntent.OnSave(
                uid(),
                orgUnitUid,
                ValueType.ORGANISATION_UNIT,
                fieldMask()
        ));
    }
}

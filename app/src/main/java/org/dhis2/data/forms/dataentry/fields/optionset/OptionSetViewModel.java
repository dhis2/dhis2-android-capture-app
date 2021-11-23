package org.dhis2.data.forms.dataentry.fields.optionset;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.ui.intent.FormIntent;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.option.Option;

import java.util.ArrayList;
import java.util.List;

@AutoValue
public abstract class OptionSetViewModel extends FieldViewModel {

    public abstract boolean isBackgroundTransparent();

    public abstract String renderType();

    public abstract ValueTypeDeviceRendering fieldRendering();

    public abstract List<Option> options();

    public abstract List<String> optionsToHide();

    public abstract List<String> optionsToShow();

    public static OptionSetViewModel create(String id,
                                            int layoutId,
                                            String label,
                                            Boolean mandatory,
                                            String optionSet,
                                            String value,
                                            String section,
                                            Boolean editable,
                                            String description,
                                            ObjectStyle objectStyle,
                                            boolean isBackgroundTransparent,
                                            String renderType,
                                            ValueTypeDeviceRendering fieldRendering,
                                            List<Option> options,
                                            ValueType valueType) {

        return new AutoValue_OptionSetViewModel(
                id,
                layoutId,
                label,
                mandatory,
                value,
                section,
                false,
                editable,
                optionSet,
                null,
                null,
                description,
                objectStyle,
                null,
                DataEntryViewHolderTypes.OPTION_SET_SELECT,
                null,
                null,
                false,
                valueType,
                isBackgroundTransparent,
                renderType,
                fieldRendering,
                options,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    @Override
    public OptionSetViewModel setMandatory() {
        return new AutoValue_OptionSetViewModel(
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
                fieldMask(),
                DataEntryViewHolderTypes.OPTION_SET_SELECT,
                style(),
                hint(),
                activated(),
                valueType(),
                isBackgroundTransparent(),
                renderType(),
                fieldRendering(),
                options(),
                optionsToHide(),
                optionsToShow()
        );
    }

    @NonNull
    @Override
    public OptionSetViewModel withEditMode(boolean isEditable) {
        return new AutoValue_OptionSetViewModel(
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
                fieldMask(),
                DataEntryViewHolderTypes.OPTION_SET_SELECT,
                style(),
                hint(),
                activated(),
                valueType(),
                isBackgroundTransparent(),
                renderType(),
                fieldRendering(),
                options(),
                optionsToHide(),
                optionsToShow()
        );
    }

    @NonNull
    @Override
    public OptionSetViewModel withError(@NonNull String error) {
        return new AutoValue_OptionSetViewModel(
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
                fieldMask(),
                DataEntryViewHolderTypes.OPTION_SET_SELECT,
                style(),
                hint(),
                activated(),
                valueType(),
                isBackgroundTransparent(),
                renderType(),
                fieldRendering(),
                options(),
                optionsToHide(),
                optionsToShow()
        );
    }

    @NonNull
    @Override
    public OptionSetViewModel withWarning(@NonNull String warning) {
        return new AutoValue_OptionSetViewModel(
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
                fieldMask(),
                DataEntryViewHolderTypes.OPTION_SET_SELECT,
                style(),
                hint(),
                activated(),
                valueType(),
                isBackgroundTransparent(),
                renderType(),
                fieldRendering(),
                options(),
                optionsToHide(),
                optionsToShow()
        );
    }

    @NonNull
    public OptionSetViewModel withOptions(@NonNull List<Option> options) {
        return new AutoValue_OptionSetViewModel(
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
                fieldMask(),
                DataEntryViewHolderTypes.OPTION_SET_SELECT,
                style(),
                hint(),
                activated(),
                valueType(),
                isBackgroundTransparent(),
                renderType(),
                fieldRendering(),
                options,
                optionsToHide(),
                optionsToShow()
        );
    }

    @NonNull
    @Override
    public OptionSetViewModel withValue(String value) {
        return new AutoValue_OptionSetViewModel(
                uid(),
                layoutId(),
                label(),
                mandatory(),
                value,
                programStageSection(),
                allowFutureDate(),
                editable(),
                optionSet(),
                warning(),
                error(),
                description(),
                objectStyle(),
                fieldMask(),
                DataEntryViewHolderTypes.OPTION_SET_SELECT,
                style(),
                hint(),
                activated(),
                valueType(),
                isBackgroundTransparent(),
                renderType(),
                fieldRendering(),
                options(),
                optionsToHide(),
                optionsToShow()
        );
    }

    @NonNull
    @Override
    public OptionSetViewModel withFocus(boolean isFocused) {
        return new AutoValue_OptionSetViewModel(
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
                fieldMask(),
                DataEntryViewHolderTypes.OPTION_SET_SELECT,
                style(),
                hint(),
                isFocused,
                valueType(),
                isBackgroundTransparent(),
                renderType(),
                fieldRendering(),
                options(),
                optionsToHide(),
                optionsToShow()
        );
    }

    @NonNull
    public FieldViewModel setOptionsToHide(List<String> optionsToHide) {
        return new AutoValue_OptionSetViewModel(
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
                fieldMask(),
                DataEntryViewHolderTypes.OPTION_SET_SELECT,
                style(),
                hint(),
                activated(),
                valueType(),
                isBackgroundTransparent(),
                renderType(),
                fieldRendering(),
                options(),
                optionsToHide != null ? optionsToHide : new ArrayList<>(),
                optionsToShow()
        );
    }

    @NonNull
    public OptionSetViewModel setOptionsToShow(List<String> optionsToShow) {
        return new AutoValue_OptionSetViewModel(
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
                fieldMask(),
                DataEntryViewHolderTypes.OPTION_SET_SELECT,
                style(),
                hint(),
                activated(),
                valueType(),
                isBackgroundTransparent(),
                renderType(),
                fieldRendering(),
                options(),
                optionsToHide(),
                optionsToShow != null ? optionsToShow : new ArrayList<>()
        );
    }

    public List<String> getOptionsToHide() {
        return optionsToHide();
    }

    public List<String> getOptionsToShow() {
        return optionsToShow();
    }

    public void onOptionSelected(String optionCode) {
        onItemClick();
        callback.intent(new FormIntent.OnSave(
                uid(),
                optionCode,
                null,
                fieldMask()
        ));
    }

    @Override
    public boolean equals(FieldUiModel o) {
        return super.equals(o) && o instanceof OptionSetViewModel &&
                this.options() == ((OptionSetViewModel) o).options() &&
                this.optionsToHide() == ((OptionSetViewModel) o).optionsToHide() &&
                this.optionsToShow() == ((OptionSetViewModel) o).optionsToShow() &&
                this.isBackgroundTransparent() == ((OptionSetViewModel) o).isBackgroundTransparent() &&
                this.renderType().equals(((OptionSetViewModel) o).renderType()) &&
                this.fieldRendering() == ((OptionSetViewModel) o).fieldRendering();
    }
}

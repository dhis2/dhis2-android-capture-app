package org.dhis2.data.forms.dataentry.fields.option_set;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.option.Option;

import java.util.ArrayList;
import java.util.List;

@AutoValue
public abstract class OptionSetViewModel extends FieldViewModel {

    private List<String> optionsToHide = new ArrayList<>();
    private List<String> optionsToShow = new ArrayList<>();

    abstract ValueTypeDeviceRendering fieldRendering();

    abstract List<Option> options();

    public static OptionSetViewModel create(String id,
                                            String label,
                                            Boolean mandatory,
                                            String optionSet,
                                            String value,
                                            String section,
                                            Boolean editable,
                                            String description,
                                            ObjectStyle objectStyle,
                                            ValueTypeDeviceRendering fieldRendering) {

        return new AutoValue_OptionSetViewModel(
                id,
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
                fieldRendering,
                new ArrayList<>()
        );
    }

    @Override
    public OptionSetViewModel setMandatory() {
        return new AutoValue_OptionSetViewModel(
                uid(),
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
                fieldRendering(),
                options()
        );
    }

    @NonNull
    @Override
    public OptionSetViewModel withEditMode(boolean isEditable) {
        return new AutoValue_OptionSetViewModel(
                uid(),
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
                fieldRendering(),
                options()
        );
    }

    @NonNull
    @Override
    public OptionSetViewModel withError(@NonNull String error) {
        return new AutoValue_OptionSetViewModel(
                uid(),
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
                fieldRendering(),
                options()
        );
    }

    @NonNull
    @Override
    public OptionSetViewModel withWarning(@NonNull String warning) {
        return new AutoValue_OptionSetViewModel(
                uid(),
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
                fieldRendering(),
                options()
        );
    }

    @NonNull
    public OptionSetViewModel withOptions(@NonNull List<Option> options) {
        return new AutoValue_OptionSetViewModel(
                uid(),
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
                fieldRendering(),
                options
        );
    }

    @NonNull
    @Override
    public OptionSetViewModel withValue(String value) {
        return new AutoValue_OptionSetViewModel(
                uid(),
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
                fieldRendering(),
                options()
        );
    }

    public void setOptionsToHide(List<String> optionsToHide) {
        if(optionsToHide!=null) {
            this.optionsToHide.addAll(optionsToHide);
        }
    }

    public void setOptionsToShow(List<String> optionsToShow) {
        if(optionsToShow!=null) {
            this.optionsToShow.addAll(optionsToShow);
        }
    }

    public List<String> getOptionsToHide() {
        return optionsToHide;
    }

    public List<String> getOptionsToShow() {
        return optionsToShow;
    }

}

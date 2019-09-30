package org.dhis2.data.forms.dataentry.fields.spinner;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class SpinnerViewModel extends FieldViewModel {

    private List<String> optionsToHide;
    private List<String> optionGroupsToHide;
    private List<String> optionGroupsToShow = new ArrayList<>();

    @NonNull
    public abstract String hint();

    @NonNull
    public abstract String optionSet();

    @NonNull
    public abstract Integer numberOfOptions();

    public static SpinnerViewModel create(String id, String label, String hintFilterOptions, Boolean mandatory,
                                          String optionSet, String value, String section, Boolean editable, String description, Integer numberOfOptions, ObjectStyle objectStyle) {
        return new AutoValue_SpinnerViewModel(id, label, mandatory, value, section, null, editable, null, null, description, objectStyle, null, hintFilterOptions, optionSet, numberOfOptions == null ? 0 : numberOfOptions);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_SpinnerViewModel(uid(), label(), true, value(), programStageSection(), allowFutureDate(), editable(), warning(), error(), description(), objectStyle(), null, hint(), optionSet(), numberOfOptions());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(), programStageSection(), allowFutureDate(), editable(), warning(), error, description(), objectStyle(), null, hint(), optionSet(), numberOfOptions());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(), programStageSection(), allowFutureDate(), editable(), warning, error(), description(), objectStyle(), null, hint(), optionSet(), numberOfOptions());
    }

    @Nonnull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), data, programStageSection(), allowFutureDate(), false, warning(), error(), description(), objectStyle(), null, hint(), optionSet(), numberOfOptions());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(), programStageSection(), allowFutureDate(), isEditable, warning(), error(), description(), objectStyle(), null, hint(), optionSet(), numberOfOptions());
    }

    public void setOptionsToHide(List<String> optionsToHide, List<String> optionsGroupsToHide) {
        this.optionGroupsToHide = new ArrayList<>();
        this.optionsToHide = new ArrayList<>();
        this.optionsToHide.addAll(optionsToHide);
        this.optionGroupsToHide.addAll(optionsGroupsToHide);
    }

    public void setOptionGroupsToShow(List<String> optionGroupsToShow){
        this.optionGroupsToShow.addAll(optionGroupsToShow);
    }

    public List<String> getOptionsToHide() {
        return optionsToHide;
    }

    public List<String> getOptionGroupsToHide() {
        return optionGroupsToHide;
    }

    public List<String> getOptionGroupsToShow() { return optionGroupsToShow; }
}

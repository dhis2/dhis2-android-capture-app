package org.dhis2.data.forms.dataentry.fields.spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.ActionType;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.hisp.dhis.android.core.common.ObjectStyle;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

@AutoValue
public abstract class SpinnerViewModel extends FieldViewModel {

    private List<String> optionsToHide;
    private List<String> optionGroupsToHide;
    private List<String> optionGroupsToShow = new ArrayList<>();

    @Nullable
    public abstract String colorByLegend();

    @NonNull
    public abstract String hint();

    @NonNull
    public abstract String optionSet();

    public static SpinnerViewModel create(String id, String label, String hintFilterOptions, Boolean mandatory,
                                          String optionSet, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, boolean isBackgroundTransparent, String renderType) {
        return new AutoValue_SpinnerViewModel(id, label, mandatory, value, section, null, editable, null, null, description, objectStyle, null, DataEntryViewHolderTypes.OPTION_SET_SPINNER, null, false, hintFilterOptions, optionSet, isBackgroundTransparent, renderType);
    }

    public static SpinnerViewModel create(String id, String label, String hintFilterOptions, Boolean mandatory,
                                          String optionSet, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, boolean isBackgroundTransparent, String renderType, FlowableProcessor<RowAction> processor) {
        return new AutoValue_SpinnerViewModel(id, label, mandatory, value, section, null, editable, null, null, description, objectStyle, null, DataEntryViewHolderTypes.OPTION_SET_SPINNER, processor, false, hintFilterOptions, optionSet, isBackgroundTransparent, renderType);
    }


    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_SpinnerViewModel(uid(), label(), true, value(), programStageSection(), allowFutureDate(), editable(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.OPTION_SET_SPINNER, processor(), activated(), hint(), optionSet(), isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(), programStageSection(), allowFutureDate(), editable(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.OPTION_SET_SPINNER, processor(), activated(), hint(), optionSet(), isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(), programStageSection(), allowFutureDate(), editable(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.OPTION_SET_SPINNER, processor(), activated(), hint(), optionSet(), isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), data, programStageSection(), allowFutureDate(), false, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.OPTION_SET_SPINNER, processor(), activated(), hint(), optionSet(), isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(), programStageSection(), allowFutureDate(), isEditable, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.OPTION_SET_SPINNER, processor(), activated(), hint(), optionSet(), isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(), programStageSection(), allowFutureDate(), editable(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.OPTION_SET_SPINNER, processor(), isFocused, hint(), optionSet(), isBackgroundTransparent(), renderType());
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(), programStageSection(), allowFutureDate(), isEditable, warning(), error(), description(), objectStyle(), null, colorByLegend(),hint(), optionSet(), numberOfOptions());
    }

    @NonNull
    public FieldViewModel withColorByLegend(String colorByLegend) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(), programStageSection(), allowFutureDate(), editable(), warning(), error(), description(), objectStyle(), null, colorByLegend, hint(), optionSet(), numberOfOptions());

    }

    public void setOptionsToHide(List<String> optionsToHide, List<String> optionsGroupsToHide) {
        this.optionGroupsToHide = new ArrayList<>();
        this.optionsToHide = new ArrayList<>();
        this.optionsToHide.addAll(optionsToHide);
        this.optionGroupsToHide.addAll(optionsGroupsToHide);
    }

    public void setOptionGroupsToShow(List<String> optionGroupsToShow) {
        this.optionGroupsToShow.addAll(optionGroupsToShow);
    }

    public List<String> getOptionsToHide() {
        return optionsToHide;
    }

    public List<String> getOptionGroupsToHide() {
        return optionGroupsToHide;
    }

    public List<String> getOptionGroupsToShow() {
        return optionGroupsToShow;
    }

    @Override
    public int getLayoutId() {
        return R.layout.form_option_set_spinner;
    }

    public abstract boolean isBackgroundTransparent();

    public abstract String renderType();

    public void onOptionSelected(String optionName, String optionCode) {
        processor().onNext(new RowAction(
                uid(),
                !isBackgroundTransparent() ? optionName + "_os_" + optionCode : optionCode,
                true,
                optionCode,
                optionName,
                null,
                null,
                ActionType.ON_SAVE
        ));
    }
}

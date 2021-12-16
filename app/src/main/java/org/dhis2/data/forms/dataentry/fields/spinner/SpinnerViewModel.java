package org.dhis2.data.forms.dataentry.fields.spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.ActionType;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.LegendValue;
import org.dhis2.form.model.RowAction;
import org.hisp.dhis.android.core.common.ObjectStyle;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;

@AutoValue
public abstract class SpinnerViewModel extends FieldViewModel {

    @Nullable
    public abstract LegendValue legendValue();

    @NonNull
    public abstract String hint();

    @NonNull
    public abstract String optionSet();

    @NonNull
    public abstract List<String> optionsToHide();

    @NonNull
    public abstract List<String> optionGroupsToHide();

    @NonNull
    public abstract List<String> optionGroupsToShow();

    public static SpinnerViewModel create(String id, String label, String hintFilterOptions,
                                          Boolean mandatory, String optionSet, String value,
                                          String section, Boolean editable, String description,
                                          ObjectStyle objectStyle, boolean isBackgroundTransparent,
                                          String renderType, LegendValue colorLegend, String url) {
        return new AutoValue_SpinnerViewModel(id, label, mandatory, value, section, null,
                editable, null, null, description, objectStyle, null,
                DataEntryViewHolderTypes.OPTION_SET_SPINNER, null, null,false,
                url, colorLegend, hintFilterOptions, optionSet,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                isBackgroundTransparent, renderType);
    }

    public static SpinnerViewModel create(String id, String label, String hintFilterOptions,
                                          Boolean mandatory, String optionSet, String value,
                                          String section, Boolean editable, String description,
                                          ObjectStyle objectStyle, boolean isBackgroundTransparent,
                                          String renderType, FlowableProcessor<RowAction> processor,
                                          LegendValue legendValue, String url) {
        return new AutoValue_SpinnerViewModel(id, label, mandatory, value, section, null,
                editable, null, null, description, objectStyle, null,
                DataEntryViewHolderTypes.OPTION_SET_SPINNER, processor, null,false,
                url, legendValue, hintFilterOptions, optionSet,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                isBackgroundTransparent, renderType);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_SpinnerViewModel(uid(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), warning(), error(), description(), objectStyle(),
                null, DataEntryViewHolderTypes.OPTION_SET_SPINNER, processor(),
                style(), activated(), url(), legendValue(), hint(), optionSet(),
                optionsToHide(), optionGroupsToHide(), optionGroupsToShow(),
                isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), warning(), error, description(), objectStyle(),
                null, DataEntryViewHolderTypes.OPTION_SET_SPINNER, processor(),
                 style(), activated(), url(), legendValue(), hint(), optionSet(),
                optionsToHide(), optionGroupsToHide(), optionGroupsToShow(),
                isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), warning, error(), description(), objectStyle(),
                null, DataEntryViewHolderTypes.OPTION_SET_SPINNER, processor(), style(),
                activated(), url(), legendValue(), hint(), optionSet(),
                optionsToHide(), optionGroupsToHide(), optionGroupsToShow(),
                isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), editable(), warning(), error(), description(), objectStyle(),
                null, DataEntryViewHolderTypes.OPTION_SET_SPINNER, processor(),
                style(), activated(), url(), legendValue(), hint(), optionSet(),
                optionsToHide(), optionGroupsToHide(), optionGroupsToShow(),
                isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), isEditable, warning(), error(), description(), objectStyle(),
                null, DataEntryViewHolderTypes.OPTION_SET_SPINNER, processor(),
                style(), activated(), url(), legendValue(), hint(), optionSet(),
                optionsToHide(), optionGroupsToHide(), optionGroupsToShow(),
                isBackgroundTransparent(), renderType());
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(),
                programStageSection(), allowFutureDate(), editable(), warning(), error(),
                description(), objectStyle(), null, dataEntryViewType(),
                processor(), style(), isFocused, url(), legendValue(), hint(), optionSet(),
                optionsToHide(), optionGroupsToHide(), optionGroupsToShow(),
                isBackgroundTransparent(), renderType());
    }

    @NonNull
    public FieldViewModel withlegendValue(LegendValue legendValue) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(),
                programStageSection(), allowFutureDate(), editable(), warning(), error(),
                description(), objectStyle(), null, dataEntryViewType(),
                processor(), style(), activated(), url(), legendValue, hint(), optionSet(),
                optionsToHide(), optionGroupsToHide(), optionGroupsToShow(),
                isBackgroundTransparent(), renderType());
    }

    @NonNull
    public FieldViewModel setOptionsToHide(List<String> optionsToHide, List<String> optionsGroupsToHide) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(),
                programStageSection(), allowFutureDate(), editable(), warning(), error(),
                description(), objectStyle(), null, dataEntryViewType(),
                processor(), style(), activated(), url(), legendValue(), hint(), optionSet(),
                optionsToHide, optionsGroupsToHide, optionGroupsToShow(),
                isBackgroundTransparent(), renderType());
    }

    @NonNull
    public FieldViewModel setOptionGroupsToShow(List<String> optionGroupsToShow) {
        return new AutoValue_SpinnerViewModel(uid(), label(), mandatory(), value(),
                programStageSection(), allowFutureDate(), editable(), warning(), error(),
                description(), objectStyle(), null, dataEntryViewType(),
                processor(), style(), activated(), url(), legendValue(), hint(), optionSet(),
                optionsToHide(), optionGroupsToHide(), optionGroupsToShow,
                isBackgroundTransparent(), renderType());
    }

    public List<String> getOptionsToHide() {
        return optionsToHide();
    }

    public List<String> getOptionGroupsToHide() {
        return optionGroupsToHide();
    }

    public List<String> getOptionGroupsToShow() {
        return optionGroupsToShow();
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

    @Override
    public boolean equals(FieldUiModel o) {
        return super.equals(o) && o instanceof SpinnerViewModel &&
                this.legendValue() == ((SpinnerViewModel) o).legendValue() &&
                this.optionSet().equals(((SpinnerViewModel) o).optionSet()) &&
                this.getOptionsToHide() == ((SpinnerViewModel) o).optionsToHide() &&
                this.optionGroupsToHide() == ((SpinnerViewModel) o).optionGroupsToHide() &&
                this.optionGroupsToShow() == ((SpinnerViewModel) o).optionGroupsToShow();
    }
}

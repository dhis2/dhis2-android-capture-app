package org.dhis2.data.forms.dataentry.fields.age;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.dhis2.Bindings.StringExtensionsKt;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.ActionType;
import org.dhis2.form.model.RowAction;
import org.dhis2.form.ui.RecyclerViewUiEvents;
import org.dhis2.form.ui.intent.FormIntent;
import org.dhis2.form.ui.style.FormUiModelStyle;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.ObjectStyle;

import java.util.Calendar;
import java.util.Date;

import io.reactivex.processors.FlowableProcessor;

@AutoValue
public abstract class AgeViewModel extends FieldViewModel {

    @NonNull
    public abstract Boolean mandatory();

    @Nullable
    public abstract String value();

    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, boolean isBackgroundTransparent, boolean isSearchMode, FlowableProcessor<RowAction> processor, FormUiModelStyle style,String url) {
        return new AutoValue_AgeViewModel(id, label, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.AGE_VIEW, processor, style,false, url, mandatory, value, isBackgroundTransparent, isSearchMode);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.AGE_VIEW, processor(), style(), activated(), url(), true, value(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.AGE_VIEW, processor(), style(), activated(), url(), mandatory(), value(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.AGE_VIEW, processor(), style(), activated(), url(), mandatory(), value(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.AGE_VIEW, processor(), style(), activated(), url(), mandatory(), data, isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), isEditable, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.AGE_VIEW, processor(), style(), activated(), url(), mandatory(), value(), isBackgroundTransparent(), isSearchMode());
    }

    @NonNull
    @Override
    public FieldViewModel withFocus(boolean isFocused) {
        return new AutoValue_AgeViewModel(uid(), label(), programStageSection(), allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.AGE_VIEW, processor(), style(), isFocused, url(), mandatory(), value(), isBackgroundTransparent(), isSearchMode());
    }

    @Override
    public int getLayoutId() {
        return R.layout.form_age_custom;
    }

    public abstract Boolean isBackgroundTransparent();

    public void onDescriptionClick(){
        callback.recyclerViewUiEvents(new RecyclerViewUiEvents.ShowDescriptionLabelDialog(label(), description()));
    }

    public void onShowCustomCalendar(){
        onItemClick();
        callback.recyclerViewUiEvents(new RecyclerViewUiEvents.OpenCustomAgeCalendar(uid(), label()));
    }

    public void onShowDayMonthYearPicker(){
        onItemClick();
        int[] yearMonthDay = valueToYearMonthDay();
        callback.recyclerViewUiEvents(new RecyclerViewUiEvents.OpenYearMonthDayAgeCalendar(uid(), yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]));
    }

    private int[] valueToYearMonthDay(){
        if (value() == null || value().isEmpty()){
            return new int[]{0,0,0};
        }

        Date initialDate = StringExtensionsKt.toDate(value());
        if (initialDate != null) {
            Calendar.getInstance().setTime(initialDate);
            return DateUtils.getDifference(initialDate, Calendar.getInstance().getTime());
        }

        return new int[]{0,0,0};
    }

    public void onAgeSet(Date ageDate) {
        if (processor() == null) return;
        processor().onNext(new RowAction(
                uid(),
                ageDate == null ? null : DateUtils.oldUiDateFormat().format(ageDate),
                false,
                null,
                null,
                null,
                null,
                ActionType.ON_SAVE)
        );
    }

    public abstract boolean isSearchMode();
}

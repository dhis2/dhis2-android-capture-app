package org.dhis2.data.forms.dataentry.fields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.KeyboardActionType;
import org.dhis2.form.model.LegendValue;
import org.dhis2.form.model.UiEventType;
import org.dhis2.form.model.UiRenderType;
import org.dhis2.form.ui.event.RecyclerViewUiEvents;
import org.dhis2.form.ui.event.UiEventFactory;
import org.dhis2.form.ui.intent.FormIntent;
import org.dhis2.form.ui.style.FormUiModelStyle;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.option.Option;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import kotlin.Pair;

public abstract class FieldViewModel implements FieldUiModel {

    @NonNull
    public abstract String uid();

    public abstract int layoutId();

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

    @NonNull
    public abstract FieldViewModel withWarning(String warning);

    @NonNull
    public abstract FieldViewModel withError(String error);

    @Nullable
    public abstract String description();

    @NonNull
    public abstract FieldViewModel withValue(String data);

    @NonNull
    public abstract FieldViewModel withEditMode(boolean isEditable);

    @NonNull
    public abstract FieldViewModel withFocus(boolean isFocused);

    @NonNull
    public abstract ObjectStyle objectStyle();

    @Nullable
    public abstract String fieldMask();

    public abstract DataEntryViewHolderTypes dataEntryViewType();

    public Callback callback;

    @Nullable
    public abstract FormUiModelStyle style();

    @Nullable
    public abstract String hint();

    @Nullable
    public Callback getCallback() {
        return callback;
    }

    @NonNull
    public abstract Boolean activated();

    @Nullable
    public abstract ValueType valueType();

    @Override
    public String getFormattedLabel() {
        if (mandatory()) {
            return label() + " *";
        } else {
            return label();
        }
    }

    public String getErrorMessage() {
        if (error() != null) {
            return error();
        } else if (warning() != null) {
            return warning();
        } else {
            return null;
        }
    }

    @Override
    public @NotNull String getUid() {
        return uid();
    }

    @Override
    public int getLayoutId() {
        return layoutId();
    }

    @Override
    @Nullable
    public FormUiModelStyle getStyle() {
        return style();
    }

    @Override
    @Nullable
    public String getHint() {
        return hint();
    }

    @Nullable
    @Override
    public String getDescription() {
        return description();
    }

    @Nullable
    @Override
    public ValueType getValueType() {
        return valueType();
    }

    @Nullable
    @Override
    public LegendValue getLegend() {
        return null;
    }

    @Override
    public boolean equals(FieldUiModel o) {
        if (o == this) {
            return true;
        }
        if (o instanceof FieldViewModel) {
            FieldViewModel that = (FieldViewModel) o;
            return this.uid().equals(that.uid())
                    && this.layoutId() == that.layoutId()
                    && this.label().equals(that.label())
                    && (this.programStageSection() == null ? that.programStageSection() == null : this.programStageSection().equals(that.programStageSection()))
                    && (this.allowFutureDate() == null ? that.allowFutureDate() == null : this.allowFutureDate().equals(that.allowFutureDate()))
                    && (this.editable() == null ? that.editable() == null : this.editable().equals(that.editable()))
                    && (this.optionSet() == null ? that.optionSet() == null : this.optionSet().equals(that.optionSet()))
                    && (this.warning() == null ? that.warning() == null : this.warning().equals(that.warning()))
                    && (this.error() == null ? that.error() == null : this.error().equals(that.error()))
                    && (this.description() == null ? that.description() == null : this.description().equals(that.description()))
                    && this.objectStyle().equals(that.objectStyle())
                    && (this.fieldMask() == null ? that.fieldMask() == null : this.fieldMask().equals(that.fieldMask()))
                    && this.dataEntryViewType().equals(that.dataEntryViewType())
                    && this.mandatory().equals(that.mandatory())
                    && (this.value() == null ? that.value() == null : this.value().equals(that.value()))
                    && (this.activated() == that.activated());
        }
        return false;
    }

    @Override
    public void setCallback(@NotNull Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onItemClick() {
        callback.intent(new FormIntent.OnFocus(
                uid(),
                value()
        ));
    }

    @Override
    public void onNext() {
        callback.intent(new FormIntent.OnNext(
                uid(),
                value(),
                null
        ));
    }

    public void onTextChange(CharSequence value) {
        String text = null;
        if (value != null) {
            text = value.toString();
        }
        callback.intent(new FormIntent.OnTextChange(
                uid(),
                text
        ));
    }

    @NotNull
    @Override
    public FieldUiModel setValue(@Nullable String value) {
        return withValue(value);
    }

    @NotNull
    @Override
    public FieldUiModel setFocus() {
        return withFocus(true);
    }

    @Nullable
    @Override
    public String getError() {
        return error();
    }

    @NotNull
    @Override
    public FieldUiModel setError(@Nullable String error) {
        return withError(error);
    }

    public FieldViewModel withLegend(LegendValue legendValue) {
        if (this instanceof SpinnerViewModel) {
            return ((SpinnerViewModel) this).withlegendValue(legendValue);
        } else {
            return this;
        }
    }

    @NotNull
    @Override
    public FieldUiModel setEditable(boolean editable) {
        return withEditMode(editable);
    }

    @NotNull
    @Override
    public FieldUiModel setLegend(@Nullable LegendValue legendValue) {
        return withLegend(legendValue);
    }

    @NonNull
    @Override
    public FieldUiModel setDisplayName(@Nullable String displayName) {
        return this;
    }

    @Nullable
    @Override
    public String getProgramStageSection() {
        return programStageSection();
    }

    @Nullable
    @Override
    public String getValue() {
        return value();
    }

    @Nullable
    @Override
    public String getOptionSet() {
        return optionSet();
    }

    @Nullable
    @Override
    public Boolean getAllowFutureDates() {
        return allowFutureDate();
    }

    @Nullable
    @Override
    public String getWarning() {
        return warning();
    }

    @NotNull
    @Override
    public FieldUiModel setWarning(@NotNull String warning) {
        return withWarning(warning);
    }

    @NotNull
    @Override
    public FieldUiModel setFieldMandatory() {
        return setMandatory();
    }

    @Override
    public boolean getMandatory() {
        return mandatory();
    }

    @NotNull
    @Override
    public String getLabel() {
        return label();
    }

    @Override
    public boolean getFocused() {
        return activated();
    }

    @Override
    public boolean getEditable() {
        return editable() != null ? editable() : true;
    }

    public void onDescriptionClick() {
        callback.recyclerViewUiEvents(new RecyclerViewUiEvents.ShowDescriptionLabelDialog(label(), description()));
    }

    @Override
    public void onClear() {
        onItemClick();
        callback.intent(new FormIntent.ClearValue(uid()));
    }

    @Override
    public void onSave(String value) {
        //Do not use until migrate to FieldUIModel
    }

    @Override
    public void onSaveBoolean(boolean b) {
        //Do not use until migrate to FieldUIModel
    }

    @Deprecated
    @Override
    public void invokeUiEvent(UiEventType uiEventType) {
        //Do not use until migrate to FieldUIModel
    }

    @Deprecated
    @Nullable
    @Override
    public UiEventFactory getUiEventFactory() {
        //Do not use until migrate to FieldUIModel
        return null;
    }

    @Deprecated
    @Nullable
    @Override
    public String getDisplayName() {
        //Do not use until migrate to FieldUIModel
        return null;
    }

    @Deprecated
    @Nullable
    @Override
    public Integer getTextColor() {
        //Do not use until migrate to FieldUIModel
        return null;
    }

    @Deprecated
    @Nullable
    @Override
    public Pair<Integer[], Integer> getBackGroundColor() {
        //Do not use until migrate to FieldUIModel
        return null;
    }

    @Deprecated
    @Nullable
    @Override
    public UiRenderType getRenderingType() {
        //Do not use until migrate to FieldUIModel
        return null;
    }

    @Deprecated
    @Override
    public void invokeIntent(@NonNull FormIntent intent) {
        //Do not use until migrate to FieldUIModel
    }

    @Deprecated
    @Nullable
    @Override
    public List<Option> getOptions() {
        //Do not use until migrate to FieldUIModel

        return null;
    }

    @Deprecated
    @Nullable
    @Override
    public List<String> getOptionsToHide() {
        //Do not use until migrate to FieldUIModel
        return null;
    }

    @Deprecated
    @Nullable
    @Override
    public List<String> getOptionsToShow() {
        //Do not use until migrate to FieldUIModel
        return null;
    }

    @Deprecated
    @Override
    public void setOptionsToHide(@Nullable List<String> optionsToHide) {
    }

    @Deprecated
    @Override
    public void setOptionsToShow(@Nullable List<String> optionsToShow) {
    }

    @Deprecated
    @Override
    public boolean getHasImage() {
        return false;
    }

    @Deprecated
    @Nullable
    @Override
    public KeyboardActionType getKeyboardActionType() {
        //Do not use until migrate to FieldUIModel
        return null;
    }

    @Nullable
    @Override
    public String getFieldMask() {
        return fieldMask();
    }

    @Deprecated
    @NonNull
    @Override
    public FieldUiModel setKeyBoardActionDone() {
        //Do not use until migrate to FieldUIModel
        return this;
    }

    @Override
    public boolean isAffirmativeChecked() {
        return false;
    }

    @Override
    public boolean isNegativeChecked() {
        return false;
    }

    @Deprecated
    @Nullable
    @Override
    public List<Option> getOptionsToDisplay() {
        //Do not use until migrate to FieldUIModel
        return null;
    }
}

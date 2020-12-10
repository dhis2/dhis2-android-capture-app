package org.dhis2.data.forms.dataentry.fields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.jetbrains.annotations.NotNull;

import io.reactivex.processors.FlowableProcessor;

public abstract class FieldViewModel implements FieldUiModel {

    @NonNull
    public abstract String uid();

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
    public abstract ObjectStyle objectStyle();

    @Nullable
    public abstract String fieldMask();

    public abstract DataEntryViewHolderTypes dataEntryViewType();

    @Nullable
    public abstract FlowableProcessor<RowAction> processor();

    public int adapterPosition = -1;

    public Callback callback;

    public boolean activated = false;

    public String getFormattedLabel() {
        if (mandatory()) {
            return label() + " *";
        } else {
            return label();
        }
    }

    public boolean shouldShowError() {
        return warning() != null || error() != null;
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

    public int getErrorAppearance() {
        if (error() != null) {
            return R.style.error_appearance;
        } else if (warning() != null) {
            return R.style.warning_appearance;
        } else {
            return -1;
        }
    }

    @Override
    public @NotNull String getUid() {
        return uid();
    }

    @Override
    public boolean equals(FieldUiModel o) {
        if (o == this) {
            return true;
        }
        if (o instanceof FieldViewModel) {
            FieldViewModel that = (FieldViewModel) o;
            return this.uid().equals(that.uid())
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
                    && (this.activated == that.activated);
        }
        return false;
    }

    @Override
    public void setCallback(@NotNull Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onActivate() {
        activated = true;
    }

    @Override
    public void onDeactivate() {
        activated = false;
    }

    public void setAdapterPosition(int index) {
        this.adapterPosition = index;
    }

    protected int getAdapterPosition() {
        return adapterPosition;
    }

    public void onItemClick() {
        callback.onClick();
    }
}

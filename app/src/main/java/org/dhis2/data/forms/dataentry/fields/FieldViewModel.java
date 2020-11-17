package org.dhis2.data.forms.dataentry.fields;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    public abstract FieldViewModel withWarning(@NonNull String warning);

    @NonNull
    public abstract FieldViewModel withError(@NonNull String error);

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

    public abstract FlowableProcessor<RowAction> processor();

    public int adapterPosition = -1;

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
                    && (this.value() == null ? that.value() == null : this.value().equals(that.value()));
        }
        return false;
    }

    public void setAdapterPosition(int index){
        this.adapterPosition = index;
    }

    public int getAdapterPosition(){
        return adapterPosition;
    }
}
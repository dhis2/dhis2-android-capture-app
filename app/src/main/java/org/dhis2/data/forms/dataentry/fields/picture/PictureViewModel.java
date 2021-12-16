package org.dhis2.data.forms.dataentry.fields.picture;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.form.model.ActionType;
import org.dhis2.form.model.RowAction;
import org.hisp.dhis.android.core.common.ObjectStyle;

import io.reactivex.processors.FlowableProcessor;

@AutoValue
public abstract class PictureViewModel extends FieldViewModel {

    public abstract boolean isBackgroundTransparent();

    public static PictureViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, FlowableProcessor<RowAction> processor, boolean isBackgroundTransparent, String url) {
        return new AutoValue_PictureViewModel(id, label, mandatory, value, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.PICTURE, processor, null,false, url, isBackgroundTransparent);
    }

    @Override
    public PictureViewModel setMandatory() {
        return new AutoValue_PictureViewModel(uid(), label(), true, value(), programStageSection(), null, editable(), null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, processor(), style(),activated(), url(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public PictureViewModel withWarning(@NonNull String warning) {
        return new AutoValue_PictureViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, processor(), style(),activated(), url(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public PictureViewModel withError(@NonNull String error) {
        return new AutoValue_PictureViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, processor(), style(),activated(), url(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public PictureViewModel withValue(String data) {
        return new AutoValue_PictureViewModel(uid(), label(), mandatory(), data, programStageSection(), null, editable(), null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, processor(), style(),activated(), url(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public PictureViewModel withEditMode(boolean isEditable) {
        return new AutoValue_PictureViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, isEditable, null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, processor(), style(),activated(), url(), isBackgroundTransparent());
    }

    @NonNull
    @Override
    public PictureViewModel withFocus(boolean isFocused) {
        return new AutoValue_PictureViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, processor(), style(),isFocused, url(), isBackgroundTransparent());
    }

    @Override
    public int getLayoutId() {
        return R.layout.custom_form_picture;
    }

    public void onClearValue() {
        processor().onNext(new RowAction(
                uid(),
                null,
                false,
                null,
                null,
                null,
                null,
                ActionType.ON_SAVE));
    }
}

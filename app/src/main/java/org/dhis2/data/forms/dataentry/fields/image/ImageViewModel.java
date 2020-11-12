package org.dhis2.data.forms.dataentry.fields.image;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.google.auto.value.AutoValue;

import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.common.ObjectStyle;

@AutoValue
public abstract class ImageViewModel extends FieldViewModel {

    public static final String NAME_CODE_DELIMITATOR = "_op_";

    public static ImageViewModel create(String id, String label, String optionSet, String value, String section, Boolean editable, Boolean mandatory, String description, ObjectStyle objectStyle) {
        return new AutoValue_ImageViewModel(id, label, mandatory, value, section, true, editable, optionSet, null, null, description, objectStyle, null, DataEntryViewHolderTypes.IMAGE);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_ImageViewModel(uid(), label(), true, value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.IMAGE);
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_ImageViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.IMAGE);
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_ImageViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), editable(), optionSet(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.IMAGE);
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_ImageViewModel(uid(), label(), mandatory(), data, programStageSection(),
                allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.IMAGE);
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_ImageViewModel(uid(), label(), mandatory(), value(), programStageSection(),
                allowFutureDate(), isEditable, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.IMAGE);
    }

    public String fieldUid() {
        return uid().split("\\.")[0];
    }

    public String optionUid() {
        return uid().split("\\.")[1];
    }

    public String fieldDisplayName() {
        return label().split(NAME_CODE_DELIMITATOR)[0];
    }

    public String optionDisplayName() {
        return label().split(NAME_CODE_DELIMITATOR)[1];
    }

    public String optionCode() {
        return label().split(NAME_CODE_DELIMITATOR)[2];
    }

    @Override
    public String getFormattedLabel() {
        if (mandatory()) {
            return optionDisplayName() + " *";
        } else {
            return optionDisplayName();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.form_image;
    }

    public void onItemClick(View view) {
        if (editable()) {
            String label = optionDisplayName();
            String code = optionCode();

            /*val value = if (imageSelector.get() == label) {
                currentSelector.set("")
                null
            } else {
                currentSelector.set(label)
                code
            }
            processor.onNext(RowAction.create(model!!.fieldUid(), value, adapterPosition))*/
        }
    }

    public Boolean isCurrentSelector() {
        //imageSelector is in the DataEntryAdapter
        /*ObservableField<String> currentSelector = imageSelector;

        Bindings.setObjectStyle(icon, itemView, viewModel.objectStyle());
        Bindings.setObjectStyle(label, itemView, viewModel.objectStyle());

        if (value() != null) {
            if (!value().equals(currentSelector.get())) {
                currentSelector.set(value());
            }
        } else if (currentSelector.get() != null) {
        }

        if (optionDisplayName().equals(currentSelector)) {
            return true;
        } else {
            return false;
        }*/
        return true;
    }

    /*
     var height: Int? = null
         val parentHeight: Int = parent.height
         sectionRendering?.let {
             when (it) {
                 ProgramStageSectionRenderingType.SEQUENTIAL.name -> {
                     height = parentHeight / if (totalFields > 2) 3 else totalFields
                 }
                 ProgramStageSectionRenderingType.MATRIX.name -> {
                     height = parentHeight / (totalFields / 2 + 1)
                 }
             }
         }

         height?.let {
             val rootView = binding.root
             val layoutParams = rootView.layoutParams
             layoutParams.height = it
             rootView.layoutParams = layoutParams
         }
     */
}

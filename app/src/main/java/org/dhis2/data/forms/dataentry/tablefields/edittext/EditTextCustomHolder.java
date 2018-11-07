package org.dhis2.data.forms.dataentry.tablefields.edittext;

import android.annotation.SuppressLint;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.Preconditions;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import io.reactivex.functions.Predicate;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;
import static java.lang.String.valueOf;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018..
 */

final class EditTextCustomHolder extends FormViewHolder {

    private final TextInputLayout inputLayout;
    private EditText editText;
    private ImageView icon;
    /* @NonNull
     private BehaviorProcessor<EditTextModel> model;*/
    EditTextModel editTextModel;

    @SuppressLint("RxLeakedSubscription")
    EditTextCustomHolder(ViewGroup parent, ViewDataBinding binding, FlowableProcessor<RowAction> processor,
                         boolean isBgTransparent, String renderType, ObservableBoolean isEditable) {
        super(binding);

        editText = binding.getRoot().findViewById(R.id.input_editText);
        icon = binding.getRoot().findViewById(R.id.renderImage);

        inputLayout = binding.getRoot().findViewById(R.id.input_layout);
        if (renderType != null && !renderType.equals(ProgramStageSectionRenderingType.LISTING.name()))
            icon.setVisibility(View.VISIBLE);

        // show and hide hint

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && editTextModel != null && editTextModel.editable()) {
                if (!isEmpty(editText.getText()) && validate())
                    processor.onNext(RowAction.create(editTextModel.uid(), editText.getText().toString()));
                else
                    processor.onNext(RowAction.create(editTextModel.uid(), null));
            }
        });

    }

    private void setInputType(ValueType valueType) {

        editText.setFocusable(editTextModel.editable());
        editText.setEnabled(editTextModel.editable());

        editText.setFilters(new InputFilter[]{});

        if (editTextModel.editable())
            switch (valueType) {
                case PHONE_NUMBER:
                    editText.setInputType(InputType.TYPE_CLASS_PHONE);
                    break;
                case EMAIL:
                    editText.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    break;
                case TEXT:
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText.setLines(1);
                    editText.setEllipsize(TextUtils.TruncateAt.END);
                    break;
                case LETTER:
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                    editText.setFilters(new InputFilter[]{
                            new InputFilter.LengthFilter(1),
                            (source, start, end, dest, dstart, dend) -> {
                                if (source.equals(""))
                                    return source;
                                if (source.toString().matches("[a-zA-Z]"))
                                    return source;
                                return "";
                            }});
                    break;
                case NUMBER:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                            InputType.TYPE_NUMBER_FLAG_DECIMAL |
                            InputType.TYPE_NUMBER_FLAG_SIGNED);
                    break;
                case INTEGER_NEGATIVE:
                case INTEGER:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    break;
                case INTEGER_ZERO_OR_POSITIVE:
                case INTEGER_POSITIVE:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setKeyListener(DigitsKeyListener.getInstance(false, false));
                    break;
                case UNIT_INTERVAL:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    break;
                case PERCENTAGE:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                case URL:
                    editText.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
                    break;
                default:
                    break;
            }
        else {
            editText.setInputType(0);
        }
    }

    @NonNull
    private Predicate<Pair<Boolean, Boolean>> valueHasChangedPredicate() {
        return state -> valueHasChanged();
    }

    @NonNull
    private Boolean valueHasChanged() {
        return !Preconditions.equals(isEmpty(editText.getText()) ? "" : editText.getText().toString(),
                editTextModel.value() == null ? "" : valueOf(editTextModel.value()));
    }

    public void update(@NonNull FieldViewModel model) {
//        model.onNext((EditTextModel) editTextModel);
        this.editTextModel = (EditTextModel) model;

        Bindings.setObjectStyle(icon, itemView, editTextModel.uid());
        editText.setEnabled(editTextModel.editable());
        editText.setText(editTextModel.value() == null ?
                null : valueOf(editTextModel.value()));

        if (!isEmpty(editTextModel.warning())) {
            inputLayout.setError(editTextModel.warning());
        } else if (!isEmpty(editTextModel.error())) {
            inputLayout.setError(editTextModel.error());
        } else
            inputLayout.setError(null);


        editText.setSelection(editText.getText() == null ?
                0 : editText.getText().length());
        if (inputLayout.getHint() == null || !inputLayout.getHint().toString().equals(editTextModel.label())) {
            label = new StringBuilder(editTextModel.label());
            if (editTextModel.mandatory())
                label.append("*");
            inputLayout.setHint(label);

            if (label.length() > 16 || model.description() != null)
                description.setVisibility(View.VISIBLE);
            else
                description.setVisibility(View.GONE);

        }

        descriptionText = editTextModel.description();
        setInputType(editTextModel.valueType());
    }

    private boolean validate() {
        switch (editTextModel.valueType()) {
            case PHONE_NUMBER:
                if (Patterns.PHONE.matcher(editText.getText().toString()).matches())
                    return true;
                else {
                    inputLayout.setError(editText.getContext().getString(R.string.invalid_phone_number));
                    return false;
                }
            case EMAIL:
                if (Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString()).matches())
                    return true;
                else {
                    inputLayout.setError(editText.getContext().getString(R.string.invalid_email));
                    return false;
                }
            case INTEGER_NEGATIVE:
                if (Integer.valueOf(editText.getText().toString()) < 0)
                    return true;
                else {
                    inputLayout.setError(editText.getContext().getString(R.string.invalid_negative_number));
                    return false;
                }
            case INTEGER_ZERO_OR_POSITIVE:
                if (Integer.valueOf(editText.getText().toString()) >= 0)
                    return true;
                else {
                    inputLayout.setError(editText.getContext().getString(R.string.invalid_possitive_zero));
                    return false;
                }
            case INTEGER_POSITIVE:
                if (Integer.valueOf(editText.getText().toString()) > 0)
                    return true;
                else {
                    inputLayout.setError(editText.getContext().getString(R.string.invalid_possitive));
                    return false;
                }
            case UNIT_INTERVAL:
                if (Float.valueOf(editText.getText().toString()) >= 0 && Float.valueOf(editText.getText().toString()) <= 1)
                    return true;
                else {
                    inputLayout.setError(editText.getContext().getString(R.string.invalid_interval));
                    return false;
                }
            case PERCENTAGE:
                if (Float.valueOf(editText.getText().toString()) >= 0 && Float.valueOf(editText.getText().toString()) <= 100)
                    return true;
                else {
                    inputLayout.setError(editText.getContext().getString(R.string.invalid_percentage));
                    return false;
                }
            default:
                return true;
        }
    }


    public void dispose() {
//        disposable.dispose();
    }
}

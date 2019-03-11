package org.dhis2.data.forms.dataentry.tablefields.edittext;

import android.annotation.SuppressLint;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.databinding.ObservableBoolean;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.ContextCompat;

import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomTextViewBinding;
import org.dhis2.databinding.CustomTextViewCellBinding;
import org.dhis2.utils.custom_views.TextInputAutoCompleteTextView;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;
import static java.lang.String.valueOf;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018..
 */

final class EditTextCellCustomHolder extends FormViewHolder {

    private EditText editText;
    private RelativeLayout relativeLayout;
    private EditTextModel editTextModel;
    private boolean accessDataWrite;

    @SuppressLint("RxLeakedSubscription")
    EditTextCellCustomHolder(CustomTextViewCellBinding binding, FlowableProcessor<RowAction> processor,
                             ObservableBoolean isEditable) {
        super(binding);
        editText = binding.editTextCell;
        relativeLayout = binding.layout;

        accessDataWrite = isEditable.get();


        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && editTextModel != null && editTextModel.editable()) {
                if (!isEmpty(editText.getText()) && validate())
                    processor.onNext(RowAction.create(editTextModel.uid(), editText.getText().toString(), editTextModel.dataElement(), editTextModel.listCategoryOption(), editTextModel.catCombo(), editTextModel.row(), editTextModel.column()));
                else
                    processor.onNext(RowAction.create(editTextModel.uid(), null, editTextModel.dataElement(), editTextModel.listCategoryOption(),editTextModel.catCombo(),editTextModel.row(), editTextModel.column()));
            }
        });

    }


    public void update(@NonNull FieldViewModel model, String value) {
        if (!model.editable()) {
            editText.setEnabled(false);
            editText.setBackgroundColor(ContextCompat.getColor(editText.getContext(), R.color.bg_black_e6e));
        } else if(accessDataWrite) {
            editText.setEnabled(true);
        }

        this.editTextModel = (EditTextModel) model;

        editText.setText(editTextModel.value() == null ?
                null : valueOf(editTextModel.value()));

        if(value != null && !value.isEmpty())
            editText.setText(value);

        editText.setSelection(editText.getText() == null ?
                0 : editText.getText().length());

        if (editTextModel.mandatory())
            label.append("*");

        descriptionText = editTextModel.description();
        binding.executePendingBindings();
        setInputType(editTextModel.valueType());
    }

    private void setInputType(ValueType valueType) {

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


    private boolean validate() {
        switch (editTextModel.valueType()) {
            case PHONE_NUMBER:
                if (Patterns.PHONE.matcher(editText.getText().toString()).matches())
                    return true;
                else {
                    editText.setError(editText.getContext().getString(R.string.invalid_phone_number));
                    return false;
                }
            case EMAIL:
                if (Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString()).matches())
                    return true;
                else {
                    editText.setError(editText.getContext().getString(R.string.invalid_email));
                    return false;
                }
            case INTEGER_NEGATIVE:
                if (Integer.valueOf(editText.getText().toString()) < 0)
                    return true;
                else {
                    editText.setError(editText.getContext().getString(R.string.invalid_negative_number));
                    return false;
                }
            case INTEGER_ZERO_OR_POSITIVE:
                if (Integer.valueOf(editText.getText().toString()) >= 0)
                    return true;
                else {
                    editText.setError(editText.getContext().getString(R.string.invalid_possitive_zero));
                    return false;
                }
            case INTEGER_POSITIVE:
                if (Integer.valueOf(editText.getText().toString()) > 0)
                    return true;
                else {
                    editText.setError(editText.getContext().getString(R.string.invalid_possitive));
                    return false;
                }
            case UNIT_INTERVAL:
                if (Float.valueOf(editText.getText().toString()) >= 0 && Float.valueOf(editText.getText().toString()) <= 1)
                    return true;
                else {
                    editText.setError(editText.getContext().getString(R.string.invalid_interval));
                    return false;
                }
            case PERCENTAGE:
                if (Float.valueOf(editText.getText().toString()) >= 0 && Float.valueOf(editText.getText().toString()) <= 100)
                    return true;
                else {
                    editText.setError(editText.getContext().getString(R.string.invalid_percentage));
                    return false;
                }
            default:
                return true;
        }
    }

    @Override
    public void dispose() {

    }
}

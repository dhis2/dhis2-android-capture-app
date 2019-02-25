package org.dhis2.utils.custom_views;

import android.app.Dialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.view.Window;
import android.widget.EditText;

import org.hisp.dhis.android.core.common.ValueType;

public class CustomViewUtils {

    private CustomViewUtils() {
        //hide public constructor
    }

    public static void setInputType(ValueType valueType, EditText editText) {
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
    }

    public static void setBgTransparent(Dialog dialog){
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}

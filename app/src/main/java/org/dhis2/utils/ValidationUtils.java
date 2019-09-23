package org.dhis2.utils;

import org.hisp.dhis.android.core.common.ValueType;

import java.util.Locale;

public class ValidationUtils {

    public static String validate(ValueType valueType, String value) {
        if (value == null)
            return null;

        String validatedValue = value;

        switch (valueType) {
            case INTEGER:
            case INTEGER_NEGATIVE:
            case INTEGER_POSITIVE:
            case INTEGER_ZERO_OR_POSITIVE:
            case PERCENTAGE:
                validatedValue = String.format(Locale.US, "%.0f", Float.valueOf(value));
                break;
            case UNIT_INTERVAL:
                validatedValue = Float.valueOf(value).toString();
                break;
            case NUMBER:
                validatedValue = String.format(Locale.US, "%.1f", Double.valueOf(value));
                break;
        }

        return validatedValue;
    }
}

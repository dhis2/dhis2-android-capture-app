package com.dhis2.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QUADRAM. Created by ppajuelo on 04/06/2018.
 */

public class StringUtils {

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}

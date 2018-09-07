package org.dhis2.utils;

import android.support.annotation.NonNull;

/**
 * Created by ppajuelo on 09/01/2018.
 *
 */

public interface CodeGenerator {

    /**
     * Generates a pseudo random string using the allowed characters. Code is
     * 11 characters long.
     */
    @NonNull
    String generate();

}

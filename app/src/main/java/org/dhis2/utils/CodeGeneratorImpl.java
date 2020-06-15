package org.dhis2.utils;

import androidx.annotation.NonNull;

import java.util.Random;

/**
 * Created by ppajuelo on 09/01/2018.
 *
 */

public class CodeGeneratorImpl implements CodeGenerator {
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String ALLOWED_CHARS = "0123456789" + LETTERS;
    private static final int NUMBER_OF_CODEPOINTS = ALLOWED_CHARS.length();
    private static final int CODESIZE = 11;

    private Random sr = new Random();

    @NonNull
    @Override
    public String generate() {
        char[] randomChars = new char[CODESIZE];

        // First char should be a letter
        randomChars[0] = LETTERS.charAt(sr.nextInt(LETTERS.length()));

        for (int i = 1; i < CODESIZE; ++i) {
            randomChars[i] = ALLOWED_CHARS.charAt(
                    sr.nextInt(NUMBER_OF_CODEPOINTS));
        }

        return new String(randomChars);
    }
}

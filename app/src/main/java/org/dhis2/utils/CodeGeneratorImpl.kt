package org.dhis2.utils

import java.util.*

class CodeGeneratorImpl: CodeGenerator {
    val LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val ALLOWED_CHARS = "0123456789$LETTERS"
    val NUMBER_OF_CODEPOINTS = ALLOWED_CHARS.length
    val CODESIZE = 11
    val random = Random()

    override fun generate(): String {
        val randomChars = CharArray(CODESIZE)
        // First char should be a letter
        randomChars[0] = LETTERS[random.nextInt(LETTERS.length)]
        for (x in 1..CODESIZE) {
            randomChars[x] = ALLOWED_CHARS[random.nextInt(NUMBER_OF_CODEPOINTS)]
        }
        return randomChars.toString()
    }

}
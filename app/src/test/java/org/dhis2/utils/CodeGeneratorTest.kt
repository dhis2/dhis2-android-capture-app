package org.dhis2.utils

import org.junit.Assert.assertTrue
import org.junit.Test

class CodeGeneratorTest {

    private val codeGenerator = CodeGeneratorImpl()

    @Test
    fun testGenerateCode() {
        val code = codeGenerator.generate()
        assertTrue(code[0].isLetter())

        for (char in code) {
            assertTrue(char.isLetterOrDigit())
        }
    }
}

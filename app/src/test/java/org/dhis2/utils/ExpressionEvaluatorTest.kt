package org.dhis2.utils

import org.apache.commons.jexl2.JexlEngine
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ExpressionEvaluatorTest {

    @Mock
    lateinit var engine: JexlEngine

    private lateinit var evaluator: ExpressionEvaluatorImpl

    @Before
    fun setUp() {
        evaluator = ExpressionEvaluatorImpl(engine)
    }

    @Test
    fun testEvaluateExpression() {
        assertEquals("expresion", evaluator.evaluate("expresion"))
    }
}

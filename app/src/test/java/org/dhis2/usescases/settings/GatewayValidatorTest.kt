package org.dhis2.usescases.settings

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class GatewayValidatorTest {

    private lateinit var validator:GatewayValidator

    @Before
    fun setup() {
        validator = GatewayValidator()
    }

    @Test
    fun `Should be considered right gateway numbers`() {
        Assert.assertTrue(validator.validate("+346709898762"))
        Assert.assertTrue(validator.validate("+5567098987621"))
        Assert.assertTrue(validator.validate("+51670989876213"))
        Assert.assertTrue(validator.validate("+896709898762133"))
        Assert.assertTrue(validator.validate("+586709"))
    }

    @Test
    fun `Should be considered invalid gateway numbers`() {
        Assert.assertFalse(validator.validate(""))
        Assert.assertFalse(validator.validate("+"))
        Assert.assertFalse(validator.validate("+34"))
        Assert.assertFalse(validator.validate("00346709898762"))
        Assert.assertFalse(validator.validate("+341231231231231231"))
        Assert.assertFalse(validator.validate("+34-11231231231"))
    }
}
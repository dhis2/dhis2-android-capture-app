package org.dhis2.usescases.settings

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class GatewayValidatorTest {
    private lateinit var validator: GatewayValidator

    @Before
    fun setup() {
        validator = GatewayValidator()
    }

    @Test
    fun `Should be considered right gateway numbers`() {
        Assert.assertEquals(GatewayValidator.GatewayValidationResult.Valid, validator("+346709898762"))
        Assert.assertEquals(GatewayValidator.GatewayValidationResult.Valid, validator("+5567098987621"))
        Assert.assertEquals(GatewayValidator.GatewayValidationResult.Valid, validator("+51670989876213"))
        Assert.assertEquals(GatewayValidator.GatewayValidationResult.Valid, validator("+896709898762133"))
        Assert.assertEquals(GatewayValidator.GatewayValidationResult.Valid, validator("+586709"))
    }

    @Test
    fun `Should be considered invalid gateway numbers`() {
        Assert.assertNotEquals(GatewayValidator.GatewayValidationResult.Valid, validator(""))
        Assert.assertNotEquals(GatewayValidator.GatewayValidationResult.Valid, validator("+"))
        Assert.assertNotEquals(GatewayValidator.GatewayValidationResult.Valid, validator("+34"))
        Assert.assertNotEquals(GatewayValidator.GatewayValidationResult.Valid, validator("00346709898762"))
        Assert.assertNotEquals(GatewayValidator.GatewayValidationResult.Valid, validator("+341231231231231231"))
        Assert.assertNotEquals(GatewayValidator.GatewayValidationResult.Valid, validator("+34-11231231231"))
    }
}

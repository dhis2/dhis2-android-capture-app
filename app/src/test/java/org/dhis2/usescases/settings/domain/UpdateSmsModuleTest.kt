package org.dhis2.usescases.settings.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.settings.GatewayValidator
import org.dhis2.usescases.settings.SettingsRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UpdateSmsModuleTest {
    lateinit var updateSmsModule: UpdateSmsModule

    val settingsRepository: SettingsRepository = mock()
    val gatewayValidator: GatewayValidator = mock()
    val settingsMessages: SettingsMessages = mock()
    val resourceManager: ResourceManager = mock()

    @Before
    fun setUp() {
        updateSmsModule =
            UpdateSmsModule(settingsRepository, gatewayValidator, settingsMessages, resourceManager)
    }

    @Test
    fun shouldReturnValidationErrorIfGatewayIsInvalid() =
        runTest {
            val testingGateway = "+11111111111"

            whenever(gatewayValidator(testingGateway)) doReturn GatewayValidator.GatewayValidationResult.Invalid

            val result = updateSmsModule(UpdateSmsModule.SmsSetting.Enable(testingGateway, 0))

            assertTrue(result is UpdateSmsModule.EnableSmsResult.ValidationError)
            assertTrue(
                (result as UpdateSmsModule.EnableSmsResult.ValidationError).validationResult ==
                    GatewayValidator.GatewayValidationResult.Invalid,
            )
        }

    @Test
    fun shouldReturnValidationErrorIfGatewayIsEmpty() =
        runTest {
            val testingGateway = "+11111111111"

            whenever(gatewayValidator(testingGateway)) doReturn GatewayValidator.GatewayValidationResult.Empty

            val result = updateSmsModule(UpdateSmsModule.SmsSetting.Enable(testingGateway, 0))

            assertTrue(result is UpdateSmsModule.EnableSmsResult.ValidationError)
            assertTrue(
                (result as UpdateSmsModule.EnableSmsResult.ValidationError).validationResult ==
                    GatewayValidator.GatewayValidationResult.Empty,
            )
        }

    @Test
    fun shouldEnableSmsModule() =
        runTest {
            val testingGateway = "+11111111111"
            val downloadingMessage = "downloading"
            val enabledMessage = "sms enabled"

            whenever(gatewayValidator(testingGateway)) doReturn GatewayValidator.GatewayValidationResult.Valid
            whenever(resourceManager.getString(R.string.sms_downloading_data)) doReturn downloadingMessage
            whenever(resourceManager.getString(R.string.sms_enabled)) doReturn enabledMessage

            val result = updateSmsModule(UpdateSmsModule.SmsSetting.Enable(testingGateway, 0))

            assertTrue(result is UpdateSmsModule.EnableSmsResult.Success)
            verify(settingsMessages, times(1)).sendMessage(downloadingMessage)
            verify(settingsRepository, times(1)).saveGatewayNumber(testingGateway)
            verify(settingsRepository, times(1)).saveSmsResponseTimeout(0)
            verify(settingsRepository, times(1)).enableSmsModule(true)
            verify(settingsMessages, times(1)).sendMessage(enabledMessage)
        }

    @Test
    fun shouldDisableSmsModule() =
        runTest {
            val testingGateway = "+11111111111"
            val downloadingMessage = "downloading"
            val disabledMessage = "sms disabled"

            whenever(gatewayValidator(testingGateway)) doReturn GatewayValidator.GatewayValidationResult.Valid
            whenever(resourceManager.getString(R.string.sms_downloading_data)) doReturn downloadingMessage
            whenever(resourceManager.getString(R.string.sms_disabled)) doReturn disabledMessage

            val result = updateSmsModule(UpdateSmsModule.SmsSetting.Disable)

            assertTrue(result is UpdateSmsModule.EnableSmsResult.Success)
            verify(settingsRepository, times(1)).enableSmsModule(false)
            verify(settingsMessages, times(1)).sendMessage(disabledMessage)
        }

    @Test
    fun shouldReturnErrorIfEnableFails() =
        runTest {
            val testingGateway = "+11111111111"
            val downloadingMessage = "downloading"
            val disabledMessage = "sms disabled"

            whenever(gatewayValidator(testingGateway)) doReturn GatewayValidator.GatewayValidationResult.Valid
            whenever(resourceManager.getString(R.string.sms_downloading_data)) doReturn downloadingMessage
            whenever(settingsRepository.enableSmsModule(true)) doThrow RuntimeException("boom")
            whenever(resourceManager.getString(R.string.sms_disabled)) doReturn disabledMessage

            val result = updateSmsModule(UpdateSmsModule.SmsSetting.Enable(testingGateway, 0))

            assertTrue(result is UpdateSmsModule.EnableSmsResult.Error)
            verify(settingsMessages, times(1)).sendMessage(downloadingMessage)
            verify(settingsMessages, times(1)).sendMessage(disabledMessage)
        }

    @Test
    fun shouldReturnErrorIfDisableFails() =
        runTest {
            val testingGateway = "+11111111111"
            val enabledMessage = "sms enabled"

            whenever(gatewayValidator(testingGateway)) doReturn GatewayValidator.GatewayValidationResult.Valid
            whenever(resourceManager.getString(R.string.sms_enabled)) doReturn enabledMessage
            whenever(settingsRepository.enableSmsModule(false)) doThrow RuntimeException("boom")

            val result = updateSmsModule(UpdateSmsModule.SmsSetting.Disable)

            assertTrue(result is UpdateSmsModule.EnableSmsResult.Error)
            verify(settingsMessages, times(1)).sendMessage(enabledMessage)
        }
}

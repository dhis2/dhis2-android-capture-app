package org.dhis2.tracker.search.ui.provider

import org.dhis2.tracker.search.ui.model.ParameterInputModel
import org.dhis2.tracker.search.ui.model.ParameterRenderingType
import org.dhis2.tracker.search.ui.model.ParameterValueType
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.model.ParameterSelectorItemModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ParameterSelectorItemProviderTest {
    @Test
    fun `should return CLOSED status when value is null`() {
        val inputModel = createTestInputModel(value = null, focused = false)

        val result = createParameterSelectorItemModel(inputModel)

        assertEquals(ParameterSelectorItemModel.Status.CLOSED, result.status)
    }

    @Test
    fun `should return CLOSED status when value is empty`() {
        val inputModel = createTestInputModel(value = "", focused = false)

        val result = createParameterSelectorItemModel(inputModel)

        assertEquals(ParameterSelectorItemModel.Status.CLOSED, result.status)
    }

    @Test
    fun `should return FOCUSED status when focused is true`() {
        val inputModel = createTestInputModel(value = "test", focused = true)

        val result = createParameterSelectorItemModel(inputModel)

        assertEquals(ParameterSelectorItemModel.Status.FOCUSED, result.status)
    }

    @Test
    fun `should return UNFOCUSED status when value exists and not focused`() {
        val inputModel = createTestInputModel(value = "test", focused = false)

        val result = createParameterSelectorItemModel(inputModel)

        assertEquals(ParameterSelectorItemModel.Status.UNFOCUSED, result.status)
    }

    @Test
    fun `should provide correct label`() {
        val expectedLabel = "Test Label"
        val inputModel = createTestInputModel(label = expectedLabel)

        val result = createParameterSelectorItemModel(inputModel)

        assertEquals(expectedLabel, result.label)
    }

    @Test
    fun `should provide correct helper text`() {
        val expectedHelper = "Optional"
        val inputModel = createTestInputModel()

        val result = createParameterSelectorItemModel(inputModel, helperText = expectedHelper)

        assertEquals(expectedHelper, result.helper)
    }

    @Test
    fun `should provide icon composable`() {
        val inputModel = createTestInputModel(valueType = ParameterValueType.TEXT)

        val result = createParameterSelectorItemModel(inputModel)

        assertNotNull(result.icon)
    }

    @Test
    fun `should provide input field composable`() {
        val inputModel = createTestInputModel()

        val result = createParameterSelectorItemModel(inputModel)

        assertNotNull(result.inputField)
    }

    @Test
    fun `should call onItemClick when expanded`() {
        var onItemClickCalled = false
        val inputModel =
            createTestInputModel(
                onItemClick = { onItemClickCalled = true },
            )

        val result = createParameterSelectorItemModel(inputModel)
        result.onExpand()

        assertEquals(true, onItemClickCalled)
    }

    @Test
    fun `should invoke QR scan request when rendering type is QR_CODE`() {
        var qrScanCalled = false
        val inputModel =
            createTestInputModel(
                renderingType = ParameterRenderingType.QR_CODE,
            )

        val result =
            createParameterSelectorItemModel(
                inputModel = inputModel,
                onQRScanRequest = { qrScanCalled = true },
            )
        result.onExpand()

        assertEquals(true, qrScanCalled)
    }

    @Test
    fun `should invoke barcode scan request when rendering type is BAR_CODE`() {
        var barcodeScanCalled = false
        val inputModel =
            createTestInputModel(
                renderingType = ParameterRenderingType.BAR_CODE,
            )

        val result =
            createParameterSelectorItemModel(
                inputModel = inputModel,
                onBarcodeScanRequest = { barcodeScanCalled = true },
            )
        result.onExpand()

        assertEquals(true, barcodeScanCalled)
    }

    @Test
    fun `should invoke QR scan request when rendering type is GS1_DATAMATRIX`() {
        var qrScanCalled = false
        val inputModel =
            createTestInputModel(
                renderingType = ParameterRenderingType.GS1_DATAMATRIX,
            )

        val result =
            createParameterSelectorItemModel(
                inputModel = inputModel,
                onQRScanRequest = { qrScanCalled = true },
            )
        result.onExpand()

        assertEquals(true, qrScanCalled)
    }

    private fun createTestInputModel(
        uid: String = "test-uid",
        label: String = "Test Label",
        value: String? = null,
        focused: Boolean = false,
        valueType: ParameterValueType? = ParameterValueType.TEXT,
        renderingType: ParameterRenderingType? = null,
        optionSet: String? = null,
        onItemClick: () -> Unit = {},
        onValueChange: (String?) -> Unit = {},
    ): ParameterInputModel =
        ParameterInputModel(
            uid = uid,
            label = label,
            value = value,
            focused = focused,
            valueType = valueType,
            renderingType = renderingType,
            optionSet = optionSet,
            onItemClick = onItemClick,
            onValueChange = onValueChange,
        )

    private fun createParameterSelectorItemModel(
        inputModel: ParameterInputModel,
        helperText: String = "Helper",
        onQRScanRequest: (() -> Unit)? = null,
        onBarcodeScanRequest: (() -> Unit)? = null,
    ): ParameterSelectorItemModel {
        val status =
            if (inputModel.focused) {
                ParameterSelectorItemModel.Status.FOCUSED
            } else if (inputModel.value.isNullOrEmpty()) {
                ParameterSelectorItemModel.Status.CLOSED
            } else {
                ParameterSelectorItemModel.Status.UNFOCUSED
            }

        return ParameterSelectorItemModel(
            icon = { ProvideParameterIcon(inputModel.valueType, inputModel.renderingType) },
            label = inputModel.label,
            helper = helperText,
            inputField = {},
            status = status,
            onExpand = {
                inputModel.onItemClick()
                when (inputModel.renderingType) {
                    ParameterRenderingType.QR_CODE,
                    ParameterRenderingType.GS1_DATAMATRIX,
                    -> onQRScanRequest?.invoke()
                    ParameterRenderingType.BAR_CODE -> onBarcodeScanRequest?.invoke()
                    else -> {}
                }
            },
        )
    }
}

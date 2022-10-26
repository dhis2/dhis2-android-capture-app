package org.dhis2.form.dialog

import com.google.zxing.BarcodeFormat
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.QRCodeWriter
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.dialog.QRImageControllerImpl
import org.junit.Assert.assertTrue
import org.junit.Test

class QRImageControllerTest {
    private val controller = QRImageControllerImpl()

    @Test
    fun shouldReturnDataMatrixWriter() {
        val testValue = "]d2\u001D01084700069915412110081996195256\u001D10DXB2005\u001D17220228"
        controller.getWriterFromRendering(testValue, UiRenderType.QR_CODE).let { (writer, format) ->
            assertTrue(writer is DataMatrixWriter)
            assertTrue(format == BarcodeFormat.DATA_MATRIX)
        }
    }

    @Test
    fun shouldReturnQRWriter() {
        val testValue = "qrValue"
        controller.getWriterFromRendering(testValue, UiRenderType.QR_CODE).let { (writer, format) ->
            assertTrue(writer is QRCodeWriter)
            assertTrue(format == BarcodeFormat.QR_CODE)
        }
    }

    @Test
    fun shouldReturnBarcodeWriter() {
        val testValue = "qrValue"
        controller.getWriterFromRendering(testValue, UiRenderType.BAR_CODE)
            .let { (writer, format) ->
                assertTrue(writer is Code128Writer)
                assertTrue(format == BarcodeFormat.CODE_128)
            }
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowException() {
        val testValue = "qrValue"
        controller.getWriterFromRendering(testValue, UiRenderType.AUTOCOMPLETE)
    }
}

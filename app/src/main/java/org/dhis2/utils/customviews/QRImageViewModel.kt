package org.dhis2.utils.customviews

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.Writer
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.QRCodeWriter
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.rules.gs1.GS1Elements

class QRImageViewModel : ViewModel() {

    private val _qrBitmap = MutableLiveData<Result<Bitmap>>()
    val qrBitmap: LiveData<Result<Bitmap>> = _qrBitmap

    fun renderQrBitmap(
        value: String,
        renderingType: UiRenderType
    ) {
        val (writer, format) = getWriterFromRendering(value, renderingType)
        if (writer != null && format != null) {
            val bitmap = writeDataToImage(value, writer, format)
            _qrBitmap.value = Result.success(bitmap)
        } else {
            _qrBitmap.value = Result.failure(Exception())
        }
    }

    private fun getWriterFromRendering(
        value: String,
        renderingType: UiRenderType
    ) = when {
        value.startsWith("]d2") -> Pair(DataMatrixWriter(), BarcodeFormat.DATA_MATRIX)
        renderingType == UiRenderType.QR_CODE -> Pair(QRCodeWriter(), BarcodeFormat.QR_CODE)
        renderingType == UiRenderType.BAR_CODE -> Pair(Code128Writer(), BarcodeFormat.CODE_128)
        else -> Pair(null, null)
    }

    private fun formattedContent(value: String) =
        value.removePrefix(GS1Elements.GS1_d2_IDENTIFIER.element)
            .removePrefix(GS1Elements.GS1_GROUP_SEPARATOR.element)

    private fun writeDataToImage(
        value: String,
        writer: Writer,
        format: BarcodeFormat
    ): Bitmap {
        val content = formattedContent(value)
        val bitMatrix = writer.encode(content, format, 500, 500)

        val width: Int = bitMatrix.width
        val height: Int = bitMatrix.height

        val bitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (i in 0 until width) {
            for (j in 0 until height) {
                bitMap.setPixel(
                    i,
                    j,
                    if (bitMatrix.get(i, j)) Color.BLACK else Color.WHITE
                )
            }
        }
        return bitMap
    }
}

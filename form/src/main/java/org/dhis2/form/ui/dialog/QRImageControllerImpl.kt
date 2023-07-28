package org.dhis2.form.ui.dialog

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.VisibleForTesting
import com.google.zxing.BarcodeFormat
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.QRCodeWriter
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.lib.expression.math.GS1Elements

class QRImageControllerImpl(
    private val qrImageSize: Int = 500,
    private val darkColor: Int = Color.BLACK,
    private val lightColor: Int = Color.WHITE
) : QRImageController {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getWriterFromRendering(value: String, renderingType: UiRenderType) = when {
        value.startsWith(GS1Elements.GS1_d2_IDENTIFIER.element) -> Pair(
            DataMatrixWriter(),
            BarcodeFormat.DATA_MATRIX
        )
        renderingType == UiRenderType.QR_CODE -> Pair(QRCodeWriter(), BarcodeFormat.QR_CODE)
        renderingType == UiRenderType.BAR_CODE -> Pair(Code128Writer(), BarcodeFormat.CODE_128)
        else -> throw IllegalArgumentException()
    }

    private fun formattedContent(value: String) =
        value.removePrefix(GS1Elements.GS1_d2_IDENTIFIER.element)
            .removePrefix(GS1Elements.GS1_GROUP_SEPARATOR.element)

    override fun writeDataToImage(value: String, renderingType: UiRenderType): Bitmap {
        val (writer, format) = getWriterFromRendering(value, renderingType)

        val content = formattedContent(value)
        val bitMatrix = writer.encode(content, format, qrImageSize, qrImageSize)

        val width: Int = bitMatrix.width
        val height: Int = bitMatrix.height

        val bitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (i in 0 until width) {
            for (j in 0 until height) {
                bitMap.setPixel(
                    i,
                    j,
                    if (bitMatrix.get(i, j)) darkColor else lightColor
                )
            }
        }
        return bitMap
    }
}

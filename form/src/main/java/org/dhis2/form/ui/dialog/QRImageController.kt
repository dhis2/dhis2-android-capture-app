package org.dhis2.form.ui.dialog

import android.graphics.Bitmap
import org.dhis2.form.model.UiRenderType

interface QRImageController {

    fun writeDataToImage(value: String, renderingType: UiRenderType): Bitmap
}

package org.dhis2.form.ui.dialog

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.dhis2.form.model.UiRenderType

class QRImageViewModel(
    private val qrController: QRImageController,
) : ViewModel() {

    private val _qrBitmap = MutableLiveData<Result<Bitmap>>()
    val qrBitmap: LiveData<Result<Bitmap>> = _qrBitmap

    fun renderQrBitmap(value: String, renderingType: UiRenderType) {
        _qrBitmap.value = try {
            val bitmap = qrController.writeDataToImage(value, renderingType)
            Result.success(bitmap)
        } catch (e: Exception) {
            Result.failure(Exception())
        }
    }
}

package org.dhis2.utils.customviews

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.QRCodeWriter
import org.dhis2.Bindings.clipWithRoundedCorners
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextViewModel
import org.dhis2.databinding.QrDetailDialogBinding
import org.dhis2.utils.ColorUtils
import org.hisp.dhis.android.core.common.ValueTypeRenderingType

class QRDetailBottomDialog(
    private val value: String,
    private val renderingType: ValueTypeRenderingType?,
    private val editable: Boolean,
    private val onClear: () -> Unit,
    private val onScan: () -> Unit
) : BottomSheetDialogFragment() {
    companion object {
        const val TAG: String = "QR_DETAIL_DIALOG"
    }

    private lateinit var binding: QrDetailDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.qr_detail_dialog, container, false)

        binding.clearButton.apply {
            setImageDrawable(
                ColorUtils.tintDrawableWithColor(
                    drawable,
                    ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY)
                )
            )
            isEnabled = editable == true
            setOnClickListener {
                onClear()
                dismiss()
            }
        }

        binding.shareButton.apply {
            setImageDrawable(
                ColorUtils.tintDrawableWithColor(
                    drawable,
                    ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY)
                )
            )
            setOnClickListener {

            }
        }

        binding.scanButton.apply {
            setImageDrawable(
                ColorUtils.tintDrawableWithColor(
                    drawable,
                    ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY)
                )
            )
            setOnClickListener {
                onScan()
                dismiss()
            }
        }

        binding.root.clipWithRoundedCorners()

        return binding.root
    }

    // This is necessary to show the bottomSheet dialog with full height on landscape
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog

            val bottomSheet =
                dialog.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet
                )
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.setPeekHeight(0)
        }
    }

    override fun onResume() {
        super.onResume()
        renderQrBitmap { qrBitmap ->
            Glide.with(this)
                .load(qrBitmap)
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .skipMemoryCache(true)
                .into(binding.fullImage)
        }
    }

    private fun renderQrBitmap(callback: (Bitmap) -> Unit) {
        val (writer, format) = when (renderingType) {
            ValueTypeRenderingType.QR_CODE -> Pair(QRCodeWriter(), BarcodeFormat.QR_CODE)
            ValueTypeRenderingType.BAR_CODE -> Pair(Code128Writer(), BarcodeFormat.CODE_128)
            else -> Pair(null, null)
        }
        if (writer != null && format != null) {
            val bitMatrix = writer.encode(value, format, 500, 500)

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
            callback.invoke(bitMap)
        } else {
            dismiss()
        }
    }
}

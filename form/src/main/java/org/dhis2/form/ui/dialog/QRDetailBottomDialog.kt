package org.dhis2.form.ui.dialog

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import org.dhis2.commons.bindings.clipWithRoundedCorners
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.form.R
import org.dhis2.form.data.FormFileProvider
import org.dhis2.form.databinding.QrDetailDialogBinding
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper
import timber.log.Timber

class
QRDetailBottomDialog(
    private val value: String,
    private val renderingType: UiRenderType?,
    private val editable: Boolean,
    private val onClear: () -> Unit,
    private val onScan: () -> Unit
) : BottomSheetDialogFragment() {
    companion object {
        const val TAG: String = "QR_DETAIL_DIALOG"
    }

    private lateinit var binding: QrDetailDialogBinding
    private var qrContentUri: Uri? = null
    private var primaryColor: Int? = -1
    private val viewModel by viewModels<QRImageViewModel> {
        QRImageViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        primaryColor = ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.qr_detail_dialog, container, false)

        binding.clearButton.apply {
            isEnabled = editable == true
            visibility = if (editable) {
                View.VISIBLE
            } else {
                View.GONE
            }
            setImageDrawable(
                ColorUtils.tintDrawableWithColor(
                    drawable,
                    primaryColor!!
                )
            )
            setOnClickListener {
                onClear()
                dismiss()
            }
        }

        binding.shareButton.apply {
            setImageDrawable(
                ColorUtils.tintDrawableWithColor(
                    drawable,
                    primaryColor!!
                )
            )
            setOnClickListener {
                qrContentUri?.let { uri ->
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setDataAndType(uri, context.contentResolver.getType(uri))
                        putExtra(Intent.EXTRA_STREAM, uri)
                        startActivity(Intent.createChooser(this, context.getString(R.string.share)))
                    }
                }
            }
        }

        binding.scanButton.apply {
            isEnabled = editable == true
            visibility = if (editable) {
                View.VISIBLE
            } else {
                View.GONE
            }
            setImageDrawable(
                ColorUtils.tintDrawableWithColor(
                    drawable,
                    primaryColor!!
                )
            )
            setOnClickListener {
                onScan()
                dismiss()
            }
        }

        binding.root.clipWithRoundedCorners()

        viewModel.qrBitmap.observe(this) { result ->
            result.fold(
                onSuccess = { renderBitmap(it) },
                onFailure = { dismiss() }
            )
        }

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
        viewModel.renderQrBitmap(value, renderingType ?: UiRenderType.QR_CODE)
    }

    private fun renderBitmap(qrBitmap: Bitmap) {
        saveQrImage(qrBitmap)
        Glide.with(this)
            .load(qrBitmap)
            .apply(RequestOptions.skipMemoryCacheOf(true))
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            .skipMemoryCache(true)
            .into(binding.fullImage)
    }

    private fun saveQrImage(qrBitmap: Bitmap) {
        try {
            val cachePath =
                FileResourceDirectoryHelper.getFileCacheResourceDirectory(requireContext())
            val stream =
                FileOutputStream("$cachePath/qrImage.png")

            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            qrContentUri = FileProvider.getUriForFile(
                requireContext(),
                FormFileProvider.fileProviderAuthority,
                File(cachePath, "qrImage.png")
            )
        } catch (e: IOException) {
            Timber.e(e)
        }
    }
}

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.commons.bindings.clipWithRoundedCorners
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.form.R
import org.dhis2.form.data.FormFileProvider
import org.dhis2.form.databinding.QrDetailDialogBinding
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonCarousel
import org.hisp.dhis.mobile.ui.designsystem.component.CarouselButtonData
import org.hisp.dhis.mobile.ui.designsystem.component.QrCodeBlock
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class
QRDetailBottomDialog(
    private val value: String,
    private val renderingType: UiRenderType?,
    private val editable: Boolean,
    private val useCompose: Boolean,
    private val onClear: () -> Unit,
    private val onScan: () -> Unit,
) : BottomSheetDialogFragment() {

    var colorUtils: ColorUtils = ColorUtils()
    companion object {
        const val TAG: String = "QR_DETAIL_DIALOG"
    }

    private lateinit var binding: QrDetailDialogBinding
    private var qrContentUri: Uri? = null
    private var primaryColor: Int? = -1
    private val viewModel by viewModels<QRImageViewModel> {
        QRImageViewModelFactory()
    }

    private var showBottomSheet: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        primaryColor = colorUtils.getPrimaryColor(context, ColorType.PRIMARY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return if (useCompose) {
            binding =
                DataBindingUtil.inflate(inflater, R.layout.qr_detail_dialog, container, false)

            viewModel.qrBitmap.observe(this) { result ->
                result.fold(
                    onSuccess = { renderBitmap(it) },
                    onFailure = { dismiss() },
                )
            }
            ComposeView(requireContext()).apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
                )
                setContent {
                    ProvideQRBottomSheet(value = value)
                }
            }
        } else {
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
                    colorUtils.tintDrawableWithColor(
                        drawable,
                        primaryColor!!,
                    ),
                )
                setOnClickListener {
                    onClear()
                    dismiss()
                }
            }

            binding.shareButton.apply {
                setImageDrawable(
                    colorUtils.tintDrawableWithColor(
                        drawable,
                        primaryColor!!,
                    ),
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
                    colorUtils.tintDrawableWithColor(
                        drawable,
                        primaryColor!!,
                    ),
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
                    onFailure = { dismiss() },
                )
            }

            binding.root
        }
    }

    @Composable
    private fun ProvideQRBottomSheet(
        modifier: Modifier = Modifier,
        value: String,

    ) {
        var showDialog by rememberSaveable(showBottomSheet) {
            mutableStateOf(showBottomSheet)
        }
        if (showDialog) {
            val buttonList = getComposeButtonList()
            BottomSheetShell(
                modifier = modifier,
                title = resources.getString(R.string.qr_code),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Button",
                        tint = SurfaceColor.Primary,
                    )
                },
                content = {
                    Row(horizontalArrangement = Arrangement.Center) {
                        // maybe add control for barcode block here
                        QrCodeBlock(data = value)
                    }
                },
                buttonBlock = {
                    ButtonCarousel(buttonList)
                },
                onDismiss = {
                    dismiss()
                    showDialog = false
                },
            )
        }
    }

    private fun getComposeButtonList(): List<CarouselButtonData> {
        val scanItem = CarouselButtonData(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = "QR scan Button",
                )
            },
            enabled = true,
            text = resources.getString(R.string.scan),
            onClick = {
                showBottomSheet = false
                onScan()
                dismiss()
            },
        )
        val buttonList = mutableListOf(
            CarouselButtonData(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "QR Share Button",
                    )
                },
                enabled = true,
                text = resources.getString(R.string.share),
                onClick = {
                    qrContentUri?.let { uri ->
                        Intent().apply {
                            action = Intent.ACTION_SEND
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            setDataAndType(uri, context?.contentResolver?.getType(uri))
                            putExtra(Intent.EXTRA_STREAM, uri)
                            startActivity(Intent.createChooser(this, context?.getString(R.string.share)))
                        }
                    }
                },
            ),
            scanItem,

            CarouselButtonData(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = "QR download Button",
                    )
                },
                enabled = true,
                text = resources.getString(R.string.download),
                onClick = {
                    qrContentUri?.let { uri ->
                        startActivity(
                            Intent().apply {
                                action = Intent.ACTION_VIEW
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                setDataAndType(uri, context?.contentResolver?.getType(uri))
                                putExtra(Intent.EXTRA_STREAM, uri)
                            },
                        )
                        // implement download action here
                    }
                },
            ),
        )
        if (!editable) {
            buttonList.remove(scanItem)
        }
        return buttonList
    }

    // This is necessary to show the bottomSheet dialog with full height on landscape
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog

            val bottomSheet =
                dialog.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet,
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
                File(cachePath, "qrImage.png"),
            )
        } catch (e: IOException) {
            Timber.e(e)
        }
    }
}

package org.dhis2.form.ui.dialog

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.form.R
import org.dhis2.form.data.FormFileProvider
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper
import org.hisp.dhis.lib.expression.math.GS1Elements
import org.hisp.dhis.mobile.ui.designsystem.component.BarcodeBlock
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
    private val label: String,
    private val value: String,
    private val renderingType: UiRenderType?,
    private val editable: Boolean,
    private val onScan: () -> Unit,
) : BottomSheetDialogFragment() {

    var colorUtils: ColorUtils = ColorUtils()

    companion object {
        const val TAG: String = "QR_DETAIL_DIALOG"
    }

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
        viewModel.qrBitmap.observe(this) { result ->
            result.fold(
                onSuccess = { saveQrImage(it) },
                onFailure = { dismiss() },
            )
        }
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                ProvideQRorBarcodeBottomSheet(
                    value = value,
                    label = label,
                )
            }
        }
    }

    @Composable
    private fun ProvideQRorBarcodeBottomSheet(
        modifier: Modifier = Modifier,
        value: String,
        label: String,

    ) {
        var showDialog by rememberSaveable(showBottomSheet) {
            mutableStateOf(showBottomSheet)
        }
        if (showDialog) {
            val buttonList = getComposeButtonList()
            BottomSheetShell(
                modifier = modifier,
                title = label,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Button",
                        tint = SurfaceColor.Primary,
                    )
                },
                content = {
                    Row(horizontalArrangement = Arrangement.Center) {
                        when (renderingType) {
                            UiRenderType.QR_CODE, UiRenderType.GS1_DATAMATRIX -> {
                                val isGS1Matrix = value.startsWith(GS1Elements.GS1_d2_IDENTIFIER.element)
                                val content = formattedContent(value)
                                QrCodeBlock(data = content, isDataMatrix = isGS1Matrix)
                            }
                            else -> {
                                BarcodeBlock(data = value)
                            }
                        }
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

    private fun formattedContent(value: String) =
        value.removePrefix(GS1Elements.GS1_d2_IDENTIFIER.element)
            .removePrefix(GS1Elements.GS1_GROUP_SEPARATOR.element)

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

    override fun onResume() {
        super.onResume()
        viewModel.renderQrBitmap(value, renderingType ?: UiRenderType.QR_CODE)
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

package org.dhis2.commons.dialogs.imagedetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.core.content.FileProvider
import org.dhis2.commons.R
import org.dhis2.commons.data.FormFileProvider
import org.dhis2.commons.extensions.getBitmap
import org.hisp.dhis.mobile.ui.designsystem.component.FullScreenImage
import timber.log.Timber
import java.io.File
import java.io.IOException

class ImageDetailActivity : AppCompatActivity() {

    companion object {

        private const val ARG_IMAGE_TITLE = "arg_image_title"
        private const val ARG_IMAGE_PATH = "arg_image_path"

        fun intent(context: Context, title: String?, imagePath: String): Intent {
            return Intent(context, ImageDetailActivity::class.java).apply {
                putExtra(ARG_IMAGE_TITLE, title)
                putExtra(ARG_IMAGE_PATH, imagePath)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra(ARG_IMAGE_TITLE)
        val imagePath = intent.getStringExtra(ARG_IMAGE_PATH)!!

        setContent {
            val painter = remember(imagePath) {
                imagePath.getBitmap()?.let { BitmapPainter(it.asImageBitmap()) }
            }

            FullScreenImage(
                painter = painter!!,
                title = title.orEmpty(),
                onDismiss = { finish() },
                onDownloadButtonClick = { saveImage(imagePath) },
                onShareButtonClick = { shareImage(imagePath) },
            )
        }
    }

    private fun saveImage(image: String) {
        // TODO: Implement saving image to disk
    }

    private fun shareImage(image: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            val contentUri = FileProvider.getUriForFile(
                this@ImageDetailActivity,
                FormFileProvider.fileProviderAuthority,
                File(image),
            )
            setDataAndType(contentUri, contentResolver.getType(contentUri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, contentUri)
        }

        val title = resources.getString(R.string.open_with)
        val chooser = Intent.createChooser(intent, title)
        try {
            startActivity(chooser)
        } catch (e: IOException) {
            Timber.e(e)
        }
    }
}

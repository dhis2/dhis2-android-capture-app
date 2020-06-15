package org.dhis2.utils.customviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import org.dhis2.Bindings.dp
import org.dhis2.Bindings.widthAndHeight
import org.dhis2.R
import org.dhis2.databinding.DetailImageBottomDialogBinding
import org.dhis2.utils.ColorUtils

class ImageDetailBottomDialog(
    val label: String?,
    private val fileToShow: File
) : BottomSheetDialogFragment() {
    companion object {
        const val TAG: String = "IMG_DETAIL_DIALOG"
    }

    private lateinit var binding: DetailImageBottomDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.detail_image_bottom_dialog, container, false)
        binding.setTitle(label)
        binding.closeButton.setImageDrawable(
            ColorUtils.tintDrawableWithColor(
                binding.closeButton.drawable,
                ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY)
            )
        )
        binding.closeButton.setOnClickListener { dismiss() }

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
        val (width, height) = fileToShow.widthAndHeight(300.dp)
        Glide.with(this)
            .load(fileToShow)
            .apply(RequestOptions.skipMemoryCacheOf(true))
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            .apply(RequestOptions.bitmapTransform(RoundedCorners(40)))
            .apply(RequestOptions().override(width, height))
            .skipMemoryCache(true)
            .into(binding.fullImage)
    }
}

package org.dhis2.commons.resources

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import org.dhis2.commons.bindings.clipWithAllRoundedCorners
import org.dhis2.commons.bindings.dp
import java.io.File
import java.util.Locale

fun ImageView.setItemPic(
    imagePath: String?,
    defaultImageRes: Int,
    defaultColorRes: Int,
    defaultValue: String?,
    isSingleEvent: Boolean = false,
    textView: TextView,
    colorUtils: ColorUtils,
) {
    when {
        imagePath?.isNotEmpty() == true -> {
            visibility = View.VISIBLE
            textView?.visibility = View.GONE
            Glide.with(context).load(File(imagePath))
                .transform(CircleCrop())
                .placeholder(defaultImageRes)
                .error(defaultImageRes)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .skipMemoryCache(true)
                .into(this)
        }
        defaultValue != null && !isSingleEvent -> {
            visibility = View.GONE
            textView?.visibility = View.VISIBLE
            textView?.clipWithAllRoundedCorners(20.dp)
            setImageDrawable(null)
            textView?.text = defaultValue.first().toString().toUpperCase(Locale.getDefault())
            textView?.setTextColor(colorUtils.getAlphaContrastColor(defaultColorRes))
            textView?.setBackgroundColor(defaultColorRes)
        }
        else -> {
            visibility = View.VISIBLE
            textView?.visibility = View.GONE
            setBackgroundColor(defaultColorRes)
            clipWithAllRoundedCorners(6.dp)
            ContextCompat.getDrawable(context, defaultImageRes)?.let {
                Glide.with(context).load(
                    colorUtils.tintDrawableReosurce(it, defaultColorRes),
                ).transform(RoundedCorners(6.dp))
                    .placeholder(defaultImageRes)
                    .error(defaultImageRes)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .skipMemoryCache(true)
                    .into(this)
            }
        }
    }
}

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
import org.dhis2.commons.bindings.dp
import java.io.File
import java.util.Locale

fun ImageView.setItemPic(
    imagePath: String?,
    defaultImageRes: Int,
    defaultColorRes: Int,
    defaultValue: String?,
    textView: TextView?
) {

    when {
        imagePath != null -> {
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
        defaultValue != null -> {
            textView?.visibility = View.VISIBLE
            setImageDrawable(null)
            textView?.text = defaultValue.first().toString().toUpperCase(Locale.getDefault())
            textView?.setTextColor(ColorUtils.getAlphaContrastColor(defaultColorRes))
            textView?.setBackgroundColor(defaultColorRes)
        }
        else -> {
            textView?.visibility = View.GONE
            setBackgroundColor(defaultColorRes)
            ContextCompat.getDrawable(context, defaultImageRes)?.let {
                Glide.with(context).load(
                    ColorUtils.tintDrawableReosurce(it, defaultColorRes)
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
package org.dhis2.form.ui.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import java.io.File
import org.dhis2.commons.bindings.dp
import org.dhis2.commons.bindings.widthAndHeight

@BindingAdapter("image_value")
fun ImageView.setImage(value: String?) {
    value?.let {
        Glide.with(this).clear(this)
        val file = File(value)
        if (file.exists()) {
            val dimensions: Pair<Int, Int> = file.widthAndHeight(200.dp)
            Glide.with(this)
                .load(file)
                .apply(RequestOptions().centerCrop())
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .apply(RequestOptions.bitmapTransform(RoundedCorners(6.dp)))
                .apply(RequestOptions.overrideOf(dimensions.component1(), dimensions.component2()))
                .skipMemoryCache(true)
                .into(this)
        }
    }
}

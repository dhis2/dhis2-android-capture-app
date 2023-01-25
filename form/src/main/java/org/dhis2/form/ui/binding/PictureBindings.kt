package org.dhis2.form.ui.binding

import android.widget.ImageView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.composethemeadapter.MdcTheme
import java.io.File
import org.dhis2.commons.bindings.dp
import org.dhis2.commons.bindings.widthAndHeight
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiRenderType
import org.dhis2.ui.IconTextButton

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

@BindingAdapter("add_button")
fun ComposeView.setButton(fieldUiModel: FieldUiModel) {
    setContent {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        MdcTheme {
            when (fieldUiModel.renderingType != UiRenderType.SIGNATURE) {
                true -> IconTextButton(
                    onClick = { fieldUiModel.invokeUiEvent(UiEventType.ADD_PICTURE) },
                    painter = painterResource(id = R.drawable.ic_add_image),
                    text = stringResource(id = R.string.add_image)
                )
                false -> IconTextButton(
                    onClick = { fieldUiModel.invokeUiEvent(UiEventType.ADD_SIGNATURE) },
                    painter = painterResource(id = R.drawable.ic_signature),
                    text = stringResource(id = R.string.add_signature)
                )
            }
        }
    }
}

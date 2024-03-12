package org.dhis2.commons.resources

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import org.dhis2.commons.R
import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.icon.Icon
import java.io.File

class MetadataIconProvider(
    private val d2: D2,
    private val resourceManager: ResourceManager,
) {
    operator fun invoke(
        style: ObjectStyle,
        @DrawableRes defaultIcon: Int = R.drawable.ic_default_outline,
        sizeInDp: Int = 40,
    ) = style.icon()?.let {
        d2.iconModule().icons().key(it).blockingGet()
    }.let { icon ->
        when {
            (icon is Icon.Custom) && !icon.path.isNullOrEmpty() -> {
                val file = File(icon.path!!)
                MetadataIconData.Custom(
                    BitmapFactory.decodeFile(file.absolutePath),
                    sizeInDp,
                )
            }

            else -> MetadataIconData.Resource(
                resourceManager.getColorOrDefaultFrom(style.color()),
                resourceManager.getObjectStyleDrawableResource(
                    style.icon(),
                    defaultIcon,
                ),
                sizeInDp,
            )
        }
    }
}

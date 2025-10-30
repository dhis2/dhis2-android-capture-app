package org.dhis2.usescases.searchTrackEntity.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import org.dhis2.commons.R
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.searchTrackEntity.SearchTeiModel
import java.io.File

fun SearchTeiModel.setTeiImage(
    context: Context,
    teiImageView: ImageView,
    teiTextImageView: TextView,
    colorUtils: ColorUtils,
    pictureListener: (String) -> Unit,
) {
    val imageBg =
        AppCompatResources.getDrawable(
            context,
            R.drawable.photo_temp_gray,
        )
    imageBg?.colorFilter =
        PorterDuffColorFilter(
            colorUtils.getPrimaryColor(
                context,
                ColorType.PRIMARY,
            ),
            PorterDuff.Mode.SRC_IN,
        )
    teiImageView.background = imageBg
    val file = File(profilePicturePath)
    val placeHolderId =
        ResourceManager(context, colorUtils)
            .getObjectStyleDrawableResource(defaultTypeIcon, -1)
    teiImageView.setOnClickListener(null)
    when {
        file.exists() -> {
            teiTextImageView.visibility = View.GONE
            Glide
                .with(context)
                .load(file)
                .error(placeHolderId)
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(CircleCrop())
                .into(teiImageView)
            teiImageView.setOnClickListener { pictureListener(profilePicturePath) }
        }

        textAttributeValues != null &&
            textAttributeValues.values.isNotEmpty() &&
            ArrayList(textAttributeValues.values)[0].value() != "-" -> {
            teiImageView.setImageDrawable(null)
            teiTextImageView.visibility = View.VISIBLE
            val valueToShow = ArrayList(textAttributeValues.values)
            if (valueToShow[0]?.value()?.isEmpty() != false) {
                teiTextImageView.text = "?"
            } else {
                teiTextImageView.text =
                    valueToShow[0]
                        .value()
                        ?.first()
                        .toString()
                        .uppercase()
            }
            teiTextImageView.setTextColor(
                colorUtils.getContrastColor(
                    colorUtils.getPrimaryColor(
                        context,
                        ColorType.PRIMARY,
                    ),
                ),
            )
        }

        isOnline &&
            attributeValues.isNotEmpty() &&
            ArrayList(attributeValues.values).firstOrNull()?.value()?.isNotEmpty() == true -> {
            teiImageView.setImageDrawable(null)
            teiTextImageView.visibility = View.VISIBLE
            val valueToShow = ArrayList(attributeValues.values)
            if (valueToShow[0] == null) {
                teiTextImageView.text = "?"
            } else {
                teiTextImageView.text =
                    valueToShow[0]
                        .value()
                        ?.first()
                        .toString()
                        .uppercase()
            }
            teiTextImageView.setTextColor(
                colorUtils.getContrastColor(
                    colorUtils.getPrimaryColor(
                        context,
                        ColorType.PRIMARY,
                    ),
                ),
            )
        }

        placeHolderId != -1 -> {
            teiTextImageView.visibility = View.GONE
            val icon =
                AppCompatResources.getDrawable(
                    context,
                    placeHolderId,
                )
            icon?.colorFilter =
                PorterDuffColorFilter(
                    colorUtils.getContrastColor(
                        colorUtils.getPrimaryColor(
                            context,
                            ColorType.PRIMARY,
                        ),
                    ),
                    PorterDuff.Mode.SRC_IN,
                )
            teiImageView.setImageDrawable(icon)
        }

        else -> {
            teiImageView.setImageDrawable(null)
            teiTextImageView.visibility = View.VISIBLE
            teiTextImageView.text = "?"
            teiTextImageView.setTextColor(
                colorUtils.getContrastColor(
                    colorUtils.getPrimaryColor(
                        context,
                        ColorType.PRIMARY,
                    ),
                ),
            )
        }
    }
}

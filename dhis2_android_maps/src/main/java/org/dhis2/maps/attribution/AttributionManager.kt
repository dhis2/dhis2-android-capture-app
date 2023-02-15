package org.dhis2.maps.attribution

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.text.style.URLSpan
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans
import com.mapbox.mapboxsdk.MapStrictMode
import com.mapbox.mapboxsdk.maps.AttributionDialogManager
import com.mapbox.mapboxsdk.maps.MapboxMap
import org.dhis2.maps.R
import org.dhis2.maps.layer.basemaps.BaseMapStyle

class AttributionManager(
    private val context: Context,
    mapboxMap: MapboxMap,
    private var currentBaseMapStyle: BaseMapStyle
) : AttributionDialogManager(context, mapboxMap) {

    override fun showAttributionDialog(attributionTitles: Array<String>) {
        val attributions: Array<String> = currentBaseMapStyle.sources.attribution.split(", ")
            .map {
                HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            }
            .toTypedArray()
        super.showAttributionDialog(attributions)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        val selectedAttribution = currentBaseMapStyle.sources.attribution.split(", ")[which]
        val url = HtmlCompat.fromHtml(selectedAttribution, HtmlCompat.FROM_HTML_MODE_LEGACY)
            .getSpans<URLSpan>().takeIf { it.isNotEmpty() }?.get(0)?.url

        if (url != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                context.startActivity(intent)
            } catch (exception: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    R.string.mapbox_attributionErrorNoBrowser,
                    Toast.LENGTH_LONG
                )
                    .show()
                MapStrictMode.strictModeViolation(exception)
            }
        }
    }

    fun updateCurrentBaseMap(updatedBaseMapStyle: BaseMapStyle) {
        currentBaseMapStyle = updatedBaseMapStyle
    }
}

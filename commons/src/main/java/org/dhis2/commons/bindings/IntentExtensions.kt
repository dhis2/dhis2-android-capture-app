package org.dhis2.commons.bindings

import androidx.fragment.app.Fragment
import org.dhis2.commons.dialogs.imagedetail.ImageDetailActivity

fun Fragment.launchImageDetail(path: String?) {
    if (!path.isNullOrBlank()) {
        val intent = ImageDetailActivity.intent(
            context = requireContext(),
            title = null,
            imagePath = path,
        )

        startActivity(intent)
    }
}

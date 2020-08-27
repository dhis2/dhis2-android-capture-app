package org.dhis2.usescases.teiDashboard

import android.content.Context
import androidx.fragment.app.Fragment
import org.dhis2.usescases.teiDashboard.createDefaultLandscapeTabFragment
import org.dhis2.usescases.teiDashboard.createDefaultPortraitTabFragment
import org.dhis2.usescases.teiDashboard.getDefaultLandscapeTabTitle
import org.dhis2.usescases.teiDashboard.getDefaultPortraitTabTitle

const val MOBILE_DASHBOARD_PORTRAIT_SIZE = 4
const val MOBILE_DASHBOARD_LANDSCAPE_SIZE = 3

fun getLandscapeTabTitle(context: Context, position: Int): String {
    return getDefaultLandscapeTabTitle(context, position)
}

fun getPortraitTabTitle(context: Context, position: Int): String {
    return getDefaultPortraitTabTitle(context, position)
}

fun createPortraitTabFragment(
    currentProgram: String?, teiUid: String,
    enrollmentUid: String?, position: Int
): Fragment {
    return createDefaultPortraitTabFragment(currentProgram, teiUid, enrollmentUid, position)
}

fun createLandscapeTabFragment(
    currentProgram: String?, teiUid: String, position: Int
): Fragment {
    return createDefaultLandscapeTabFragment(currentProgram, teiUid, position)
}

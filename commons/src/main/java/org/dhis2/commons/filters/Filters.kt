package org.dhis2.commons.filters

import androidx.annotation.LayoutRes
import org.dhis2.commons.R

enum class Filters(@LayoutRes val layoutId: Int) {
    PERIOD(R.layout.item_filter_period),
    ORG_UNIT(R.layout.item_filter_org_unit),
    SYNC_STATE(R.layout.item_filter_state),
    CAT_OPT_COMB(R.layout.item_filter_cat_opt_comb),
    EVENT_STATUS(R.layout.item_filter_status),
    ASSIGNED_TO_ME(R.layout.item_filter_assigned),
    ENROLLMENT_DATE(R.layout.item_filter_period),
    ENROLLMENT_STATUS(R.layout.item_filter_enrollment_status),
    WORKING_LIST(R.layout.item_filter_working_list),
    FOLLOW_UP(R.layout.item_filter_followup),
    NON(-1),
}

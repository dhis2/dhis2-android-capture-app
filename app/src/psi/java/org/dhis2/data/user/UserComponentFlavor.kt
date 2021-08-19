package org.dhis2.data.user

import org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback.FeedbackComponent
import org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback.FeedbackModule

interface UserComponentFlavor {
    fun plus(feedbackModule: FeedbackModule): FeedbackComponent
 }
package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import dagger.Subcomponent
import org.dhis2.data.dagger.PerActivity

@PerActivity
@Subcomponent(modules = [FeedbackModule::class])
interface FeedbackComponent {
    fun inject(feedbackActivity: FeedbackActivity)
    fun inject(feedbackContentFragment: FeedbackContentFragment) {
    }
}

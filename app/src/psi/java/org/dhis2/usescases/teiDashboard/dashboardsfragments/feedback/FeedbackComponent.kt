package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

import dagger.Subcomponent
import org.dhis2.data.dagger.PerFragment

@PerFragment
@Subcomponent(modules = [FeedbackModule::class])
interface FeedbackComponent {
    fun inject(feedbackFragment: FeedbackFragment)
    fun inject(feedbackFragment: FeedbackContentFragment) {
    }
}

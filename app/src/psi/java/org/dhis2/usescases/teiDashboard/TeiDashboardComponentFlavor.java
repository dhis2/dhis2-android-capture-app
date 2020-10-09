package org.dhis2.usescases.teiDashboard;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback.FeedbackComponent;
import org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback.FeedbackModule;
import org.jetbrains.annotations.NotNull;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = TeiDashboardModule.class)
public interface TeiDashboardComponentFlavor extends TeiDashboardComponent {
    @NotNull FeedbackComponent plus(FeedbackModule feedbackModule);
}

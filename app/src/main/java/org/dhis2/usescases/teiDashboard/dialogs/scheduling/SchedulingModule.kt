package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.ProgramStage

@Module
class SchedulingModule(
    val enrollment: Enrollment,
    val programStages: List<ProgramStage>,
) {
    @Provides
    @PerFragment
    fun provideSchedulingViewModelFactory(
        d2: D2,
        resourceManager: ResourceManager,
        periodUtils: DhisPeriodUtils,
    ): SchedulingViewModelFactory =
        SchedulingViewModelFactory(enrollment, programStages, d2, resourceManager, periodUtils)
}

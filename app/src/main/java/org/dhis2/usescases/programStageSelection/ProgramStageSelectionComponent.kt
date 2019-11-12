package org.dhis2.usescases.programStageSelection

import dagger.Subcomponent
import org.dhis2.data.dagger.PerActivity

/**
 * Created by ppajuelo on 30/11/2017.
 *
 */
@PerActivity
@Subcomponent(modules = [ProgramStageSelectionModule::class])
interface ProgramStageSelectionComponent {
    fun inject(programStageSelectionActivity: ProgramStageSelectionActivity)
}

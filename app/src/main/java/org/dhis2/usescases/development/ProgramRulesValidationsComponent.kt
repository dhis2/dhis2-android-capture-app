package org.dhis2.usescases.development

import dagger.Subcomponent
import org.dhis2.commons.di.dagger.PerActivity

@PerActivity
@Subcomponent(modules = [ProgramRulesValidationsModule::class])
interface ProgramRulesValidationsComponent {
    fun inject(activity: ProgramRulesValidationActivity)
}

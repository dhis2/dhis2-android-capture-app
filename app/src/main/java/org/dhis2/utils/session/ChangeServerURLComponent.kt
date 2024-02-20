package org.dhis2.utils.session

import dagger.Subcomponent

@Subcomponent(modules = [ChangeServerURLModule::class])
interface ChangeServerURLComponent {
    fun inject(changeServerUrlDialog: ChangeServerUrlDialog)
}

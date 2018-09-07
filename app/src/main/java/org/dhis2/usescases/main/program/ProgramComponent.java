package org.dhis2.usescases.main.program;

import org.dhis2.data.dagger.PerFragment;

import dagger.Subcomponent;

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */
@PerFragment
@Subcomponent(modules = ProgramModule.class)
public interface ProgramComponent {
    void inject(ProgramFragment programFragment);
}

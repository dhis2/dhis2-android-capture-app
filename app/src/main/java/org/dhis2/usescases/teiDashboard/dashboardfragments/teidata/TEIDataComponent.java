package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata;

import org.dhis2.data.dagger.PerFragment;

import dagger.Subcomponent;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
@PerFragment
@Subcomponent(modules = TEIDataModule.class)
public interface TEIDataComponent {

    void inject(TEIDataFragment notesFragment);

}

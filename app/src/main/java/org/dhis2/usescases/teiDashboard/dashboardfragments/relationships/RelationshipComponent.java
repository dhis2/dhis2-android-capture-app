package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import org.dhis2.commons.di.dagger.PerFragment;

import dagger.Subcomponent;

@PerFragment
@Subcomponent(modules = RelationshipModule.class)
public interface RelationshipComponent {

    void inject(RelationshipFragment relationshipFragment);

}

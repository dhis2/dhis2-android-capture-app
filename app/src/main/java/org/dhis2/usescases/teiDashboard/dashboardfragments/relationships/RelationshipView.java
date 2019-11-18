package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.relationship.RelationshipType;

import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public interface RelationshipView extends AbstractActivityContracts.View {

        void setRelationships(List<RelationshipViewModel> relationships);

        void setRelationshipTypes(List<Trio<RelationshipType, String, Integer>> relationshipTypes);

        void goToAddRelationship(String teiTypeToAdd, String teiUid);

        void goToTeiDashboard(String teiUid);

        void showDialogRelationshipWithoutEnrollment(String displayName);

        void showDialogRelationshipNotFoundMessage(String displayName);

}

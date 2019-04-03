package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.List;

import androidx.lifecycle.LiveData;
import io.reactivex.Observable;

public interface RelationshipPresenter extends TeiDashboardContracts.Presenter {

    LiveData<DashboardProgramModel> observeDashboardModel();

    void subscribeToRelationships(RelationshipFragment relationshipFragment);

    void subscribeToRelationshipTypes(RelationshipFragment relationshipFragment);

    void goToAddRelationship(String teiTypeToAdd);

    void addRelationship(String trackEntityInstanceA, String relationshipType);

    Observable<List<TrackedEntityAttributeValueModel>> getTEIMainAttributes(String teiUid);

    void openDashboard(String teiUid);

    void deleteRelationship(Relationship relationshipModel);
}

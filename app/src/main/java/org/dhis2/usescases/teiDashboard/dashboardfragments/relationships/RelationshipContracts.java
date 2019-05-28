package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import android.content.Intent;

import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public class RelationshipContracts {

    public interface View extends AbstractActivityContracts.View {

        Consumer<List<RelationshipViewModel>> setRelationships();

        Consumer<List<Trio<RelationshipTypeModel, String, Integer>>> setRelationshipTypes();

        void goToAddRelationship(Intent intent);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void init(View view);

        void goToAddRelationship(String teiTypeToAdd);

        void deleteRelationship(Relationship relationship);

        void addRelationship(String trackEntityInstance_A, String relationshipType);

        void openDashboard(String teiUid);

        Observable<List<TrackedEntityAttributeValueModel>> getTEIMainAttributes(String teiUid);

        String getTeiUid();
    }

}

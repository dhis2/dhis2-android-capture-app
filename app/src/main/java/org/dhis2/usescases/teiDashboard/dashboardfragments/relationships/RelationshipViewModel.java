package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;

import java.util.List;

@AutoValue
public abstract class RelationshipViewModel {

    public enum RelationshipDirection {FROM, TO}

    public abstract Relationship relationship();

    public abstract Geometry fromGeometry();

    public abstract Geometry toGeometry();

    public abstract RelationshipType relationshipType();

    public abstract RelationshipDirection relationshipDirection();

    public abstract String teiUid();

    public abstract List<TrackedEntityAttributeValue> teiAttributes();

    public static RelationshipViewModel create(Relationship relationship,
                                               RelationshipType relationshipType,
                                               RelationshipDirection relationshipDirection,
                                               String teiUid,
                                               List<TrackedEntityAttributeValue> attributeValues,
                                               Geometry fromGeometry,
                                               Geometry toGeometry) {
        return new AutoValue_RelationshipViewModel(relationship, relationshipType, relationshipDirection, teiUid, attributeValues, fromGeometry, toGeometry);
    }

}

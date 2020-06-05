package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships;

import android.graphics.drawable.Drawable;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;

import java.util.List;

import javax.annotation.Nullable;

@AutoValue
public abstract class RelationshipViewModel {

    public enum RelationshipDirection {FROM, TO}

    public abstract Relationship relationship();

    @Nullable
    public abstract Geometry fromGeometry();

    @Nullable
    public abstract Geometry toGeometry();

    public abstract RelationshipType relationshipType();

    public abstract RelationshipDirection relationshipDirection();

    public abstract String teiUid();

    public abstract List<TrackedEntityAttributeValue> fromAttributes();

    public abstract List<TrackedEntityAttributeValue> toAttributes();

    public abstract String fromImage();

    public abstract String toImage();

    public abstract int fromDefaultImageResource();

    public abstract int toDefaultImageResource();

    public static RelationshipViewModel create(Relationship relationship,
                                               RelationshipType relationshipType,
                                               RelationshipDirection relationshipDirection,
                                               String teiUid,
                                               List<TrackedEntityAttributeValue> fromAttributes,
                                               List<TrackedEntityAttributeValue> toAttributes,
                                               Geometry fromGeometry,
                                               Geometry toGeometry,
                                               String fromImage,
                                               String toImage,
                                               int fromDefaultImage,
                                               int toDefaultImage) {
        return new AutoValue_RelationshipViewModel(relationship, fromGeometry, toGeometry,
                relationshipType, relationshipDirection, teiUid, fromAttributes, toAttributes,
                fromImage, toImage, fromDefaultImage, toDefaultImage);
    }

}

package org.dhis2.data.forms.dataentry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.reactivex.Flowable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.data.forms.dataentry.DataEntryStore.valueType.ATTR;
import static org.dhis2.data.forms.dataentry.DataEntryStore.valueType.DATA_ELEMENT;

public final class AttributeValueStore implements DataEntryStore {

    @NonNull
    private final String enrollment;
    private final D2 d2;
    private final EnrollmentObjectRepository enrollmentRepository;

    public AttributeValueStore(D2 d2, @NonNull String enrollment) {
        this.enrollment = enrollment;
        this.d2 = d2;
        this.enrollmentRepository = d2.enrollmentModule().enrollments().uid(enrollment);
    }

    @NonNull
    @Override
    public Flowable<Long> save(@NonNull String uid, @Nullable String value) {
        return Flowable.just(getValueType(uid))
                .filter(valueType -> currentValue(uid, valueType, value))
                .switchMap(valueType -> {
                    if (isEmpty(value))
                        return Flowable.just(delete(uid, valueType));
                    else {
                        long updated = update(uid, value, valueType);
                        if (updated > 0) {
                            return Flowable.just(updated);
                        } else
                            return Flowable.just(insert(uid, value, valueType));
                    }
                });
    }

    private long update(@NonNull String attribute, @Nullable String value, valueType valueType) {
        if (valueType == ATTR) {
            try {
                d2.trackedEntityModule().trackedEntityAttributeValues().value(attribute,
                        enrollmentRepository.blockingGet().trackedEntityInstance()).blockingSet(value);
                return 1;
            } catch (D2Error d2Error) {
                Timber.e(d2Error);
                return -1;
            }

        } else {
            String eventUid = eventUid(attribute);
            try {
                d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, attribute).blockingSet(value);
                return 1;
            } catch (D2Error d2Error) {
                Timber.e(d2Error);
                return -1;
            }
        }
    }

    private valueType getValueType(@Nonnull String uid) {
        return d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingExists() ? ATTR : DATA_ELEMENT;
    }

    private boolean currentValue(@NonNull String uid, valueType valueType, String currentValue) {
        String value;
        if (currentValue != null && (currentValue.equals("0.0") || currentValue.isEmpty()))
            currentValue = null;

        if (valueType == ATTR) {
            TrackedEntityAttributeValueObjectRepository attrValueRepository =
                    d2.trackedEntityModule().trackedEntityAttributeValues().value(uid,
                            enrollmentRepository.blockingGet().trackedEntityInstance());
            value = attrValueRepository.blockingExists() ? attrValueRepository.blockingGet().value() : null;

        } else {
            TrackedEntityDataValueObjectRepository dataValueRepository =
                    d2.trackedEntityModule().trackedEntityDataValues().value(eventUid(uid), uid);
            value = dataValueRepository.blockingExists() ? dataValueRepository.blockingGet().value() : null;
        }

        return !Objects.equals(value, currentValue);
    }

    private long insert(@NonNull String attribute, @NonNull String value, valueType valueType) {
        if (valueType == ATTR) {
            String teiUid = enrollmentRepository.blockingGet().trackedEntityInstance();
            try {
                d2.trackedEntityModule().trackedEntityAttributeValues().value(attribute, teiUid)
                        .blockingSet(value);
                return 1;
            } catch (D2Error d2Error) {
                Timber.e(d2Error);
                return -1;
            }
        } else {
            String eventUid = eventUid(attribute);
            try {
                d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, attribute)
                        .blockingSet(value);
                return 1;
            } catch (D2Error d2Error) {
                Timber.e(d2Error);
                return -1;
            }
        }
    }

    private long delete(@NonNull String attribute, valueType valueType) {
        if (valueType == ATTR) {
            try {
                d2.trackedEntityModule().trackedEntityAttributeValues().value(attribute,
                        enrollmentRepository.blockingGet().trackedEntityInstance()).blockingSet(null);
                d2.trackedEntityModule().trackedEntityAttributeValues().value(attribute,
                        enrollmentRepository.blockingGet().trackedEntityInstance()).blockingDelete();
                return 1;
            } catch (D2Error d2Error) {
                d2Error.printStackTrace();
                return -1;
            }
        } else {
            String eventUid = eventUid(attribute);
            try {
                d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, attribute).blockingSet(null);
                d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, attribute).blockingDelete();
                return 1;
            } catch (D2Error d2Error) {
                d2Error.printStackTrace();
                return -1;
            }
        }
    }

    private String eventUid(String attribute) {
        String eventUid = "";
        List<Event> events = d2.eventModule().events().byEnrollmentUid().eq(enrollment)
                .byStatus().eq(EventStatus.ACTIVE)
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC).blockingGet();

        List<TrackedEntityDataValue> dataValues = d2.trackedEntityModule().trackedEntityDataValues()
                .byDataElement().eq(attribute)
                .byEvent().in(UidsHelper.getUidsList(events))
                .blockingGet();

        if (dataValues != null && !dataValues.isEmpty())
            eventUid = dataValues.get(0).event();

        return eventUid;
    }
}

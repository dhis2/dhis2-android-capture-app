package org.dhis2.data.forms.dataentry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.event.EventObjectRepository;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueObjectRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;

import java.util.Objects;

import javax.annotation.Nonnull;

import io.reactivex.Flowable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.data.forms.dataentry.DataEntryStore.valueType.ATTR;
import static org.dhis2.data.forms.dataentry.DataEntryStore.valueType.DATA_ELEMENT;

public final class DataValueStore implements DataEntryStore {

    @NonNull
    private final String eventUid;
    private final D2 d2;
    private final EventObjectRepository eventRepository;
    private final EnrollmentObjectRepository enrollmentRepository;

    public DataValueStore(@NonNull D2 d2,
                          @NonNull String eventUid) {
        this.d2 = d2;
        this.eventUid = eventUid;
        this.eventRepository = d2.eventModule().events().uid(eventUid);
        if (eventRepository.blockingGet().enrollment() != null)
            this.enrollmentRepository = d2.enrollmentModule().enrollments().uid(eventRepository.blockingGet().enrollment());
        else
            this.enrollmentRepository = null;

    }

    @NonNull
    @Override
    public Flowable<Long> save(@NonNull String uid, @Nullable String value) {
        return Flowable.fromCallable(() -> getValueType(uid))
                .filter(valueType -> currentValue(uid, valueType, value))
                .switchMap(valueType -> {
                    if (isEmpty(value))
                        return Flowable.just(delete(uid, valueType));
                    else {
                        long updated = update(uid, value, valueType);
                        if (updated > 0)
                            return Flowable.just(updated);
                        else
                            return Flowable.just(insert(uid, value, valueType));
                    }
                });
    }

    @NonNull
    @Override
    public Flowable<Boolean> checkUnique(@NonNull String uid, @Nullable String value) {
        if (value != null && getValueType(uid) == ATTR) {
            boolean isUnique = Boolean.TRUE.equals(d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet().unique());
            if (isUnique && !d2.trackedEntityModule().trackedEntityAttributeValues()
                    .byTrackedEntityAttribute().eq(uid)
                    .byValue().eq(value).blockingGet().isEmpty()) {
                delete(uid, ATTR);
                return Flowable.just(false);
            } else
                return Flowable.just(true);
        } else
            return Flowable.just(true);
    }


    private long update(@NonNull String uid, @Nullable String value, valueType valueType) {
        if (valueType == ATTR) {
            try {
                d2.trackedEntityModule().trackedEntityAttributeValues().value(uid,
                        enrollmentRepository.blockingGet().trackedEntityInstance()).blockingSet(value);
                return 1;
            } catch (D2Error d2Error) {
                Timber.e(d2Error);
                return -1;
            }

        } else {
            try {
                d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, uid).blockingSet(value);
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
                    d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, uid);
            value = dataValueRepository.blockingExists() ? dataValueRepository.blockingGet().value() : null;
        }

        return !Objects.equals(value, currentValue);
    }

    private long insert(@NonNull String uid, @Nullable String value, valueType valueType) {
        if (valueType == ATTR) {
            String teiUid = enrollmentRepository.blockingGet().trackedEntityInstance();
            try {
                d2.trackedEntityModule().trackedEntityAttributeValues().value(uid, teiUid)
                        .blockingSet(value);
                return 1;
            } catch (D2Error d2Error) {
                Timber.e(d2Error);
                return -1;
            }
        } else {
            try {
                d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, eventUid)
                        .blockingSet(value);
                return 1;
            } catch (D2Error d2Error) {
                Timber.e(d2Error);
                return -1;
            }
        }
    }

    private long delete(@NonNull String uid, valueType valueType) {
        if (valueType == ATTR) {
            try {
                d2.trackedEntityModule().trackedEntityAttributeValues().value(uid,
                        enrollmentRepository.blockingGet().trackedEntityInstance()).blockingSet(null);
                d2.trackedEntityModule().trackedEntityAttributeValues().value(uid,
                        enrollmentRepository.blockingGet().trackedEntityInstance()).blockingDelete();
                return 1;
            } catch (D2Error d2Error) {
                d2Error.printStackTrace();
                return -1;
            }
        } else {
            try {
                d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, uid).blockingSet(null);
                d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, uid).blockingDelete();
                return 1;
            } catch (D2Error d2Error) {
                d2Error.printStackTrace();
                return -1;
            }
        }
    }
}

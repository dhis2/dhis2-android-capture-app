package org.dhis2.usescases.programEventDetail;

import com.google.auto.value.AutoValue;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventStatus;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * QUADRAM. Created by ppajuelo on 31/01/2019.
 */
@AutoValue
public abstract class ProgramEventViewModel {

    @NonNull
    public abstract String uid();

    @NonNull
    public abstract String orgUnitUid();

    @NonNull
    public abstract String orgUnitName();

    @NonNull
    public abstract Date date();

    @NonNull
    public abstract State eventState();

    @NonNull
    public abstract List<Pair<String, String>> eventDisplayData();

    @NonNull
    public abstract EventStatus eventStatus();

    @NonNull
    public abstract Boolean isExpired();
    @NonNull
    public abstract String attributeOptionComboName();

    @NonNull
    public static ProgramEventViewModel create(@NonNull String uid, @NonNull String orgUnitUid, @NonNull String orgUnitName, @NonNull Date date,
                                               @NonNull State eventState, @NonNull List<Pair<String, String>> data, @NonNull EventStatus status,
                                               @NonNull Boolean isExpired, @NonNull String attributeOptionComboName) {
        return new AutoValue_ProgramEventViewModel(uid, orgUnitUid, orgUnitName, date, eventState, data, status, isExpired,attributeOptionComboName);
    }

}

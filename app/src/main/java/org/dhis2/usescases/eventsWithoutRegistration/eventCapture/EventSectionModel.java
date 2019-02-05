package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

/**
 * QUADRAM. Created by ppajuelo on 21/11/2018.
 */
@AutoValue
public abstract class EventSectionModel {

    @NonNull
    public abstract String sectionName();

    @NonNull
    public abstract String sectionUid();

    @NonNull
    public abstract Integer numberOfCompletedFields();

    @NonNull
    public abstract Integer numberOfTotalFields();

    @NonNull
    public static EventSectionModel create(@NonNull String sectionName,
                                           @NonNull String sectionUid,
                                           @NonNull Integer numberOfCompletedFields,
                                           @NonNull Integer numberOfTotalFields) {
        return new AutoValue_EventSectionModel(sectionName, sectionUid, numberOfCompletedFields, numberOfTotalFields);
    }
}

package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EventInfoSectionsViewModel {

    // uid of Event
    @NonNull
    public abstract String uid();

    // uid of ProgramStageSection
    @NonNull
    public abstract String sectionUid();

    // name of section
    @NonNull
    public abstract String sectionName();

    @NonNull
    static EventInfoSectionsViewModel create(@NonNull String eventUid, @NonNull String programStageUid, @NonNull String sectionName) {
        return new AutoValue_EventInfoSectionsViewModel(eventUid, programStageUid, sectionName);
    }
}

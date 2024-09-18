package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import static org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragmentKt.VISUALIZATION_TYPE;

import android.os.Bundle;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.dhis2.R;
import org.dhis2.form.model.EventMode;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.EventCaptureFormFragment;
import org.dhis2.usescases.notes.NotesFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.VisualizationType;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EventCapturePagerAdapter extends FragmentStateAdapter {

    private final String programUid;
    private final String eventUid;
    private final List<EventPageType> pages;

    private final boolean shouldOpenErrorSection;

    private final EventMode eventMode;

    public boolean isFormScreenShown(@Nullable Integer currentItem) {
        return currentItem != null && pages.get(currentItem) == EventPageType.DATA_ENTRY;
    }

    private enum EventPageType {
        DATA_ENTRY, ANALYTICS, RELATIONSHIPS, NOTES
    }

    public EventCapturePagerAdapter(FragmentActivity fragmentActivity,
                                    String programUid,
                                    String eventUid,
                                    boolean displayAnalyticScreen,
                                    boolean displayRelationshipScreen,
                                    boolean openErrorSection,
                                    EventMode eventMode

    ) {
        super(fragmentActivity);
        this.programUid = programUid;
        this.eventUid = eventUid;
        this.shouldOpenErrorSection = openErrorSection;
        this.eventMode = eventMode;
        pages = new ArrayList<>();
        pages.add(EventPageType.DATA_ENTRY);

        if (displayAnalyticScreen) {
            pages.add(EventPageType.ANALYTICS);
        }

        if (displayRelationshipScreen) {
            pages.add(EventPageType.RELATIONSHIPS);
        }
        pages.add(EventPageType.NOTES);
    }

    public int getDynamicTabIndex(@IntegerRes int tabClicked) {
        if (tabClicked == R.id.navigation_data_entry) {
            return pages.indexOf(EventPageType.DATA_ENTRY);
        } else if (tabClicked == R.id.navigation_analytics) {
            return pages.indexOf(EventPageType.ANALYTICS);
        } else if (tabClicked == R.id.navigation_relationships) {
            return pages.indexOf(EventPageType.RELATIONSHIPS);
        } else if (tabClicked == R.id.navigation_notes) {
            return pages.indexOf(EventPageType.NOTES);
        }
        return 0;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (pages.get(position)) {
            default:
            case DATA_ENTRY:
                return EventCaptureFormFragment.newInstance(
                        eventUid,
                        shouldOpenErrorSection,
                        eventMode
                );
            case ANALYTICS:
                Fragment indicatorFragment = new IndicatorsFragment();
                Bundle arguments = new Bundle();
                arguments.putString(VISUALIZATION_TYPE, VisualizationType.EVENTS.name());
                indicatorFragment.setArguments(arguments);
                return indicatorFragment;
            case RELATIONSHIPS:
                Fragment relationshipFragment = new RelationshipFragment();
                relationshipFragment.setArguments(
                        RelationshipFragment.withArguments(programUid,
                                null,
                                null,
                                eventUid
                        )
                );
                return relationshipFragment;
            case NOTES:
                return NotesFragment.newEventInstance(programUid, eventUid);
        }
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }
}

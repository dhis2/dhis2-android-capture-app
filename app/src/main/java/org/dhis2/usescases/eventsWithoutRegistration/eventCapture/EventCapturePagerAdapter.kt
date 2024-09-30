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
    private final List<EventPageType> landscapePages;
    private final List<EventPageType> portraitPages;

    private final FragmentActivity fragmentActivity;

    private final boolean shouldOpenErrorSection;

    private final EventMode eventMode;

    public static final int NO_POSITION = -1;

    public boolean isFormScreenShown(@Nullable Integer currentItem) {
        return currentItem != null && portraitPages.get(currentItem) == EventPageType.DATA_ENTRY;
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
        this.fragmentActivity = fragmentActivity;
        landscapePages = new ArrayList<>();
        portraitPages = new ArrayList<>();

        portraitPages.add(EventPageType.DATA_ENTRY);

        if (displayAnalyticScreen) {
            portraitPages.add(EventPageType.ANALYTICS);
            landscapePages.add(EventPageType.ANALYTICS);
        }

        if (displayRelationshipScreen) {
            portraitPages.add(EventPageType.RELATIONSHIPS);
            landscapePages.add(EventPageType.RELATIONSHIPS);
        }
        portraitPages.add(EventPageType.NOTES);
        landscapePages.add(EventPageType.NOTES);
    }

    public int getDynamicTabIndex(@IntegerRes int tabClicked) {
        EventPageType pageType = switch (tabClicked) {
            case R.id.navigation_analytics -> EventPageType.ANALYTICS;
            case R.id.navigation_relationships -> EventPageType.RELATIONSHIPS;
            case R.id.navigation_notes -> EventPageType.NOTES;
            default -> null;
        };

        if (pageType != null) {
            if (isPortrait()) {
                return portraitPages.indexOf(pageType);
            } else {
                return landscapePages.indexOf(pageType);
            }
        } else {
            return NO_POSITION;
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return createFragmentForPage(
                isPortrait() ?
                        portraitPages.get(position) :
                        landscapePages.get(position)
        );
    }

    private Fragment createFragmentForPage(EventPageType pageType) {
        switch (pageType) {
            case ANALYTICS -> {
                Fragment indicatorFragment = new IndicatorsFragment();
                Bundle arguments = new Bundle();
                arguments.putString(VISUALIZATION_TYPE, VisualizationType.EVENTS.name());
                indicatorFragment.setArguments(arguments);
                return indicatorFragment;
            }
            case RELATIONSHIPS -> {
                Fragment relationshipFragment = new RelationshipFragment();
                relationshipFragment.setArguments(
                        RelationshipFragment.withArguments(programUid,
                                null,
                                null,
                                eventUid
                        )
                );
                return relationshipFragment;
            }
            case NOTES -> {
                return NotesFragment.newEventInstance(programUid, eventUid);
            }
            default -> {
                return EventCaptureFormFragment.newInstance(
                        eventUid,
                        shouldOpenErrorSection,
                        eventMode
                );
            }
        }

    }

    @Override
    public int getItemCount() {
        if (isPortrait()) {
            return portraitPages.size();
        } else {
            return landscapePages.size();
        }
    }

    public boolean isPortrait() {
        return fragmentActivity.getResources().getConfiguration().orientation == 1;
    }
}

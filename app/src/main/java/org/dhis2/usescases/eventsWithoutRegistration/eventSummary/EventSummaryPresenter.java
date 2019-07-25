package org.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.sharedPreferences.SharePreferencesProvider;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventSummaryPresenter implements EventSummaryContract.Presenter {

    static private EventSummaryContract.View view;
    private final EventSummaryContract.Interactor interactor;
    private final SharePreferencesProvider provider;


    EventSummaryPresenter(EventSummaryContract.Interactor interactor, SharePreferencesProvider provider) {
        this.interactor = interactor;
        this.provider = provider;
    }

    @Override
    public void init(@NonNull EventSummaryContract.View mview, @NonNull String programId, @NonNull String eventId) {
        view = mview;
        view.setPreferences(provider);
        interactor.init(view, programId, eventId);
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void getSectionCompletion(@Nullable String sectionUid) {
        interactor.getSectionCompletion(sectionUid);
    }

    @Override
    public void onDoAction() {
        interactor.onDoAction();
    }

    @Override
    public void doOnComple() {
        interactor.doOnComple();
    }

    @Override
    public void onDettach() {
        interactor.onDettach();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }
}

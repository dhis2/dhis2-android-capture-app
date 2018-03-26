package com.dhis2.usescases.eventsWithoutRegistration.eventSummary;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventSummaryPresenter implements EventSummaryContract.Presenter {

    static private EventSummaryContract.View view;
    private final EventSummaryContract.Interactor interactor;


    EventSummaryPresenter(EventSummaryContract.Interactor interactor) {
        this.interactor = interactor;
    }

    @Override
    public void init(EventSummaryContract.View mview, String programId, String eventId) {
        view = mview;
        interactor.init(view, programId, eventId);
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onDettach() {
        interactor.onDettach();
    }
}

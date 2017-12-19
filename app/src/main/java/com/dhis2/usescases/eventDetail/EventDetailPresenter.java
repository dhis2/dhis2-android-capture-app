package com.dhis2.usescases.eventDetail;

import com.dhis2.data.metadata.MetadataRepository;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by ppajuelo on 19/12/2017.
 */

public class EventDetailPresenter implements EventDetailContracts.Presenter {

    private final MetadataRepository metadataRepository;
    private final D2 d2;
    private EventDetailContracts.View view;

    @Inject
    public EventDetailPresenter(D2 d2, MetadataRepository metadataRepository) {

        this.d2 = d2;
        this.metadataRepository = metadataRepository;
    }

    @Override
    public void init(EventDetailContracts.View view) {
        this.view = view;
    }

    @Override
    public void getEventData(String eventUid) {
        d2.retrofit().create(EventService.class).trackEntityInstances(eventUid).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                EventModel eventModel = EventModel.builder()
                        .uid(response.body().uid())
                        .enrollmentUid(response.body().enrollmentUid())
                        .created(response.body().created())
                        .lastUpdated(response.body().lastUpdated())
                        .createdAtClient(response.body().createdAtClient())
                        .lastUpdatedAtClient(response.body().lastUpdatedAtClient())
                        .program(response.body().program())
                        .programStage(response.body().programStage())
                        .organisationUnit(response.body().organisationUnit())
                        .eventDate(response.body().eventDate())
                        .status(response.body().status())
                        .completedDate(response.body().completedDate())
                        .dueDate(response.body().dueDate())
                        .build();

                List<TrackedEntityDataValueModel> dataValueModelList = new ArrayList<>();
                for (TrackedEntityDataValue dataValue : response.body().trackedEntityDataValues()) {
                    dataValueModelList.add(
                            TrackedEntityDataValueModel.builder()
                                    .event(eventUid)
                                    .dataElement(dataValue.dataElement())
                                    .storedBy(dataValue.storedBy())
                                    .value(dataValue.value())
                                    .created(dataValue.created())
                                    .lastUpdated(dataValue.lastUpdated())
                                    .providedElsewhere(dataValue.providedElsewhere())
                                    .build()
                    );
                }

                view.setData(eventModel, dataValueModelList, metadataRepository);

            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {

            }
        });
    }

    interface EventService {
        @GET("28/events/{eventUid}")
        Call<Event> trackEntityInstances(@Path("eventUid") String eventUid);
    }
}

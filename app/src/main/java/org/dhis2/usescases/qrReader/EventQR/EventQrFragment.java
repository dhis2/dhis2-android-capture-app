package org.dhis2.usescases.qrReader.EventQR;


import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.dhis2.data.metadata.MetadataRepository;

import org.dhis2.R;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.main.MainActivity;

import org.hisp.dhis.android.core.event.EventModel;

import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventQrFragment extends FragmentGlobalAbstract {

    private static MetadataRepository metadataRepository;
    private static Activity activity;

    @Override
    public void onAttach(Activity activity) {
        activity = activity;
        super.onAttach(activity);
    }

    EventModel eventModel;
    public EventQrFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_qr, container, false);
        // event = getActivity().getIntent().getParcelableExtra("EVENT_UID");
        EventModel.Builder builder= EventModel.builder()
                .uid("ySRaK6iu4Wd")
                .eventDate(new Date())
                .dueDate(new Date())
                .organisationUnit("DiszpKrYNg8")
                .programStage("lST1OZ5BDJ2")
                .attributeOptionCombo("bRowv6yZOF2");

        eventModel = builder.build();
       /* getStageName(eventModel.programStage());*/
       if(activity==null){
           activity = (MainActivity)getActivity();
       }

      /*  binding.setEvent(eventModel);*/

      changeTitle("EVENTO QR");




        return binding.getRoot();
    }

    public static void getStageName( String stageId) {
        Disposable disposable= new CompositeDisposable();
        disposable = metadataRepository.programStage(stageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        programStageModel -> changeTitle(programStageModel.displayName()),
                        Timber::d
                );
    }

    public static void changeTitle(String text){
        ((MainActivity)activity).setTitle(text);
    }

}

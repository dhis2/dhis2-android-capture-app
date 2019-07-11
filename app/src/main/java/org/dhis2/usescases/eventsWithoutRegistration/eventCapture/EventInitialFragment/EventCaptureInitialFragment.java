package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventInitialFragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.EventCaptureInitialFragmentBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.general.FragmentGlobalAbstract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

/**
 * QUADRAM. Created by ppajuelo on 08/04/2019.
 */
public class EventCaptureInitialFragment extends FragmentGlobalAbstract {
    private static EventCaptureInitialFragment instance;
    private EventCaptureActivity activity;
    private EventCaptureInitialFragmentBinding binding;

    public static EventCaptureInitialFragment getInstance() {
        if (instance == null) {
            instance = new EventCaptureInitialFragment();
        }
        return instance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (EventCaptureActivity) context;
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.event_capture_initial_fragment, container, false);
        return binding.getRoot();
    }
}

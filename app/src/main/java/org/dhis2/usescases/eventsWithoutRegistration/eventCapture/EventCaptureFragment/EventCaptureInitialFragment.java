package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.dhis2.R;
import org.dhis2.databinding.EventCaptureInitialFragmentBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.jetbrains.annotations.NotNull;

/**
 * QUADRAM. Created by ppajuelo on 08/04/2019.
 */
public class EventCaptureInitialFragment extends FragmentGlobalAbstract {
    private static EventCaptureInitialFragment instance;

    public static EventCaptureInitialFragment getInstance() {
        if (instance == null) {
            instance = new EventCaptureInitialFragment();
        }
        return instance;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventCaptureInitialFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.event_capture_initial_fragment, container, false);
        return binding.getRoot();
    }
}

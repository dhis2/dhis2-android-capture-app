package com.dhis2.usescases.qrReader.EventQR;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.main.MainActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventQrFragment extends FragmentGlobalAbstract {


    public EventQrFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_qr, container, false);
        assert ((MainActivity)getActivity()) != null;
        ((MainActivity)getActivity()).setTitle("Evento escaneado");
        return binding.getRoot();
    }

}

package com.dhis2.usescases.teiDashboard.dashboardfragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.FragmentNotesBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.teiDashboard.adapters.NotesAdapter;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class NotesFragment extends FragmentGlobalAbstract {
    FragmentNotesBinding binding;
    static NotesFragment instance;

    static public NotesFragment getInstance() {
        if (instance == null)
            instance = new NotesFragment();

        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notes, container, false);
        binding.notesRecycler.setAdapter(new NotesAdapter());
        binding.buttonAdd.setOnClickListener(this::addNote);
        binding.buttonDelete.setOnClickListener(this::clearNote);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    public void addNote(View view) {
        ((NotesAdapter) binding.notesRecycler.getAdapter()).addNote(binding.editNote.getText().toString());
        clearNote(view);
    }

    public void clearNote(View view) {
        binding.editNote.getText().clear();
    }

}

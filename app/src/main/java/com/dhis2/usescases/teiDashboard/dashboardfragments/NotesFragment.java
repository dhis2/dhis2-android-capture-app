package com.dhis2.usescases.teiDashboard.dashboardfragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.FragmentNotesBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import com.dhis2.usescases.teiDashboard.adapters.NotesAdapter;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;

import org.hisp.dhis.android.core.enrollment.note.NoteModel;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class NotesFragment extends FragmentGlobalAbstract {
    FragmentNotesBinding binding;
    static NotesFragment instance;
    private NotesAdapter noteAdapter;
    TeiDashboardContracts.Presenter presenter;

    static public NotesFragment getInstance() {
        if (instance == null)
            instance = new NotesFragment();

        return instance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter = ((TeiDashboardMobileActivity) context).getPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notes, container, false);
        noteAdapter = new NotesAdapter();
        presenter.setNoteProcessor(noteAdapter.asFlowable());
        binding.notesRecycler.setAdapter(noteAdapter);
        binding.buttonAdd.setOnClickListener(this::addNote);
        binding.buttonDelete.setOnClickListener(this::clearNote);
        return binding.getRoot();
    }

    public void addNote(View view) {
        noteAdapter.addNote(binding.editNote.getText().toString());
        clearNote(view);
    }

    public void clearNote(View view) {
        binding.editNote.getText().clear();
    }

    public Consumer<List<NoteModel>> swapNotes() {
        return noteModels -> {
            noteAdapter.setItems(noteModels);
        };
    }


}

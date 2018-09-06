package org.dhis2.usescases.teiDashboard.dashboardfragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.FragmentNotesBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import org.dhis2.usescases.teiDashboard.adapters.NotesAdapter;
import org.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;

import org.hisp.dhis.android.core.enrollment.note.NoteModel;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class NotesFragment extends FragmentGlobalAbstract {
    FragmentNotesBinding binding;
    static NotesFragment instance;
    private NotesAdapter noteAdapter;
    TeiDashboardContracts.Presenter presenter;
    ActivityGlobalAbstract activity;

    static public NotesFragment getInstance() {
        if (instance == null)
            instance = new NotesFragment();

        return instance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (ActivityGlobalAbstract) context;
        presenter = ((TeiDashboardMobileActivity) context).getPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notes, container, false);
        noteAdapter = new NotesAdapter();
        presenter.setNoteProcessor(noteAdapter.asFlowable());
        presenter.subscribeToNotes(this);
        binding.notesRecycler.setAdapter(noteAdapter);
        binding.buttonAdd.setOnClickListener(this::addNote);
        binding.buttonDelete.setOnClickListener(this::clearNote);
        return binding.getRoot();
    }

    public void addNote(View view) {
        if (presenter.hasProgramWritePermission()) {
            noteAdapter.addNote(binding.editNote.getText().toString());
            clearNote(view);
        } else
            activity.displayMessage(getString(R.string.search_access_error));
    }

    public void clearNote(View view) {
        binding.editNote.getText().clear();
    }

    public Consumer<List<NoteModel>> swapNotes() {
        return noteModels -> {
            noteAdapter.setItems(noteModels);
        };
    }


    public static Fragment createInstance() {
        return instance = new NotesFragment();
    }
}

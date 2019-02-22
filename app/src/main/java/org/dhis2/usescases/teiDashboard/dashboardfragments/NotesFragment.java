package org.dhis2.usescases.teiDashboard.dashboardfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class NotesFragment extends FragmentGlobalAbstract {
    private FragmentNotesBinding binding;
    private static NotesFragment instance;
    private NotesAdapter noteAdapter;
    private TeiDashboardContracts.TeiDashboardPresenter presenter;
    private ActivityGlobalAbstract activity;

    public static NotesFragment getInstance() {
        if (instance == null)
            instance = new NotesFragment();

        return instance;
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
        binding.buttonAdd.setOnClickListener(view -> addNote());
        binding.buttonDelete.setOnClickListener(view -> clearNote());
        binding.editNote.setOnTouchListener((v, event) -> {
            if (v.getId() == R.id.edit_note) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                    default:
                        break;
                }
            }
            return false;
        });
        return binding.getRoot();
    }

    private void addNote() {
        if (presenter.hasProgramWritePermission()) {
            noteAdapter.addNote(binding.editNote.getText().toString());
            clearNote();
        } else
            activity.displayMessage(getString(R.string.search_access_error));
    }

    private void clearNote() {
        binding.editNote.getText().clear();
    }

    public Consumer<List<NoteModel>> swapNotes() {
        return noteModels -> noteAdapter.setItems(noteModels);
    }

    public static Fragment createInstance() {
        instance = new NotesFragment();
        return instance;
    }
}

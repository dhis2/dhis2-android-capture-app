package org.dhis2.usescases.teiDashboard.dashboardfragments.notes;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.CREATE_NOTE;

import java.util.List;

import javax.inject.Inject;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.FragmentNotesBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.hisp.dhis.android.core.note.Note;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class NotesFragment extends FragmentGlobalAbstract implements NotesContracts.View {

    @Inject
    NotesContracts.Presenter presenter;

    FragmentNotesBinding binding;
    private NotesAdapter noteAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        TeiDashboardMobileActivity activity = (TeiDashboardMobileActivity) context;
        if (((App) context.getApplicationContext()).dashboardComponent() != null)
            ((App) context.getApplicationContext())
                    .dashboardComponent()
                    .plus(new NotesModule(activity.getProgramUid(), activity.getTeiUid()))
                    .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notes, container, false);
        noteAdapter = new NotesAdapter();
        binding.notesRecycler.setAdapter(noteAdapter);
        binding.editNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.buttonDelete.setVisibility(isEmpty(s) ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.addNoteButton.setOnClickListener(this::addNote);
        binding.buttonDelete.setOnClickListener(this::clearNote);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init(this);
        presenter.setNoteProcessor(noteAdapter.asFlowable());
        presenter.subscribeToNotes();
    }

    @Override
    public void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    public void addNote(View view) {
        if (presenter.hasProgramWritePermission()) {
            analyticsHelper().setEvent(CREATE_NOTE, CLICK, CREATE_NOTE);
//            noteAdapter.addNote(binding.editNote.getText().toString());
            presenter.saveNote(binding.editNote.getText().toString());
            clearNote(view);
        } else
            displayMessage(getString(R.string.search_access_error));
    }

    public void clearNote(View view) {
        binding.editNote.getText().clear();
    }

    @Override
    public Consumer<List<Note>> swapNotes() {
        return noteModels -> noteAdapter.setItems(noteModels);
    }
}

package org.dhis2.usescases.teiDashboard.adapters;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemNotesBinding;

import org.hisp.dhis.android.core.enrollment.note.NoteModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * Created by Administrador on 18/12/2017.
 */

public class NotesAdapter extends RecyclerView.Adapter<NotesViewholder> {

    private List<NoteModel> notes;
    private final FlowableProcessor<Pair<String, Boolean>> processor;

    public NotesAdapter() {
        this.processor = PublishProcessor.create();
        this.notes = new ArrayList<>();
    }

    @Override
    public NotesViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemNotesBinding itemNotesBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_notes, parent, false);
        return new NotesViewholder(itemNotesBinding);
    }

    @Override
    public void onBindViewHolder(NotesViewholder holder, int position) {
        holder.bind(notes.get(position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void addNote(String noteText) {
        processor.onNext(Pair.create(noteText, true));
    }

    public void setItems(List<NoteModel> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    public FlowableProcessor<Pair<String, Boolean>> asFlowable() {
        return processor;
    }
}

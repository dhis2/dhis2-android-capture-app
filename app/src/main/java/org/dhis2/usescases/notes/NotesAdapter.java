package org.dhis2.usescases.notes;

import java.util.ArrayList;
import java.util.List;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemNotesBinding;
import org.hisp.dhis.android.core.note.Note;
import org.jetbrains.annotations.NotNull;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * QUADRAM. Created by Administrador on 18/12/2017.
 */

public class NotesAdapter extends RecyclerView.Adapter<NotesViewholder> {

    private List<Note> notes;
    private final FlowableProcessor<Pair<String, Boolean>> processor;

    public NotesAdapter() {
        this.processor = PublishProcessor.create();
        this.notes = new ArrayList<>();
    }

    @NotNull
    @Override
    public NotesViewholder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        ItemNotesBinding itemNotesBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_notes, parent, false);
        return new NotesViewholder(itemNotesBinding);
    }

    @Override
    public void onBindViewHolder(@NotNull NotesViewholder holder, int position) {
        holder.bind(notes.get(position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setItems(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    public FlowableProcessor<Pair<String, Boolean>> asFlowable() {
        return processor;
    }
}

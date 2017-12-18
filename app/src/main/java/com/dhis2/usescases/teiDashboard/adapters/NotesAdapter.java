package com.dhis2.usescases.teiDashboard.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.ItemNotesBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrador on 18/12/2017.
 */

public class NotesAdapter extends RecyclerView.Adapter<NotesViewholder> {

    List<String> notes;//TODO: CHANGE WHEN SDK IS UPDATED WITH NOTES MODELS

    public NotesAdapter() {
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
        notes.add(noteText);
       notifyDataSetChanged();
    }
}

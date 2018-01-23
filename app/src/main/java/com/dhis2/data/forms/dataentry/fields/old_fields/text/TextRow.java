package com.dhis2.data.forms.dataentry.fields.old_fields.text;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.hisp.dhis.android.dataentry.R;
import org.hisp.dhis.android.dataentry.form.dataentry.fields.Row;

public final class TextRow implements Row<TextViewHolder, TextViewModel> {

    @NonNull
    private final LayoutInflater inflater;

    public TextRow(@NonNull LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @NonNull
    @Override
    public TextViewHolder onCreate(@NonNull ViewGroup parent) {
        return new TextViewHolder(inflater.inflate(
                R.layout.recyclerview_row_textview, parent, false));
    }

    @Override
    public void onBind(@NonNull TextViewHolder viewHolder, @NonNull TextViewModel viewModel) {
        viewHolder.update(viewModel);
    }
}
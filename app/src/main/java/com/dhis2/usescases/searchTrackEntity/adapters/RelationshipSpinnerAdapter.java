package com.dhis2.usescases.searchTrackEntity.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;

import java.util.List;

/**
 * Created by frodriguez on 6/6/2018.
 */
public class RelationshipSpinnerAdapter extends ArrayAdapter<RelationshipTypeModel> {

    private List<RelationshipTypeModel> itemList;

    public RelationshipSpinnerAdapter(@NonNull Context context, @NonNull List<RelationshipTypeModel> itemList) {
        super(context, android.R.layout.simple_spinner_dropdown_item, itemList);
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            row = layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        TextView textView = row.findViewById(android.R.id.text1);
        textView.setText(itemList.get(position).aIsToB());

        return row;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            row = layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        TextView textView = row.findViewById(android.R.id.text1);
        textView.setText(itemList.get(position).aIsToB());

        return row;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0;
    }
}

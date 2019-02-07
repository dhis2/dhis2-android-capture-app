package org.dhis2.usescases.searchTrackEntity.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.ColorUtils;

import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * QUADRAM. Created by frodriguez on 6/6/2018.
 */
public class RelationshipSpinnerAdapter extends ArrayAdapter<Pair<RelationshipTypeModel, String>> {

    private List<Pair<RelationshipTypeModel, String>> data;

    public RelationshipSpinnerAdapter(@NonNull Context context, @NonNull List<RelationshipTypeModel> itemList) {
        super(context, android.R.layout.simple_spinner_dropdown_item);
        this.data = new ArrayList<>();
        this.data.add(Pair.create(RelationshipTypeModel.builder().build(), context.getString(R.string.add_relation_button)));
        for (RelationshipTypeModel relationshipTypeModel : itemList) {
            data.add(Pair.create(relationshipTypeModel, relationshipTypeModel.displayName()));
//            if (relationshipTypeModel.aIsToB().equals(relationshipTypeModel.bIsToA()))
//                data.add(Trio.create(relationshipTypeModel, relationshipTypeModel.aIsToB(), true));
//            else {
//                data.add(Trio.create(relationshipTypeModel, relationshipTypeModel.aIsToB(), true)); //for aIsToB
//                data.add(Trio.create(relationshipTypeModel, relationshipTypeModel.bIsToA(), false)); //for bIsToA
//            }
        }
        addAll(data);
        notifyDataSetChanged();

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
        textView.setTextColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.ACCENT));
        textView.setText(data.get(position).val1());

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
        if (position > 0)
            textView.setTextColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY_DARK));
        else {
            textView.setTextColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.ACCENT));
            textView.setBackgroundColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY));
        }
        textView.setText(data.get(position).val1());

        return row;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0;
    }
}

package com.dhis2.usescases.main;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dhis2.R;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

/**
 * Created by ppajuelo on 19/10/2017.
 */

public class OrgUnitHolder extends TreeNode.BaseNodeViewHolder<OrganisationUnit> {

    private TextView textView;
    private ImageView imageView;

    public OrgUnitHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, OrganisationUnit value) {
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View view = layoutInflater.inflate(R.layout.item_node, null, false);
        textView = view.findViewById(R.id.org_unit_name);
        imageView = view.findViewById(R.id.org_unit_icon);
        int textSize = 21 - (value.level());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        textView.setText(value.shortName());
        return view;
    }

    @Override
    public void toggle(boolean active) {
        imageView.setImageResource(active ? R.drawable.ic_remove_circle : R.drawable.ic_add_circle);
    }
}

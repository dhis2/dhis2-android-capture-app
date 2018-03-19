package com.dhis2.usescases.main.program;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.dhis2.R;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

/**
 * Created by ppajuelo on 19/10/2017.
 */

public class OrgUnitHolder extends TreeNode.BaseNodeViewHolder<OrganisationUnitModel> {

    private TextView textView;
    private ImageView imageView;
    private CheckBox checkBox;
    private TreeNode node;

    public OrgUnitHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, OrganisationUnitModel value) {
        this.node = node;
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View view = layoutInflater.inflate(R.layout.item_node, null, false);
        textView = view.findViewById(R.id.org_unit_name);
        imageView = view.findViewById(R.id.org_unit_icon);
        int textSize = 21 - (value.level());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        textView.setText(value.displayName());
        checkBox = view.findViewById(R.id.checkbox);
        checkBox.setChecked(node.isSelected());


        if (!node.isSelectable()) {
            checkBox.setVisibility(View.GONE);
            textView.setTextColor(ContextCompat.getColor(textView.getContext(),R.color.gray_814));
        }else {
            node.setSelected(true);
            textView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            checkBox.setChecked(true);
        }


        if (node.getChildren() == null || node.getChildren().isEmpty())
            imageView.setImageResource(R.drawable.ic_circle);

        /*checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            textView.setTextColor(b ? ContextCompat.getColor(context, R.color.colorPrimary) : ContextCompat.getColor(context, R.color.gray_444));
            node.setSelected(b);
        });*/

        return view;
    }

    @Override
    public void toggle(boolean active) {
        if (((OrganisationUnitModel) mNode.getValue()).level() != 4)
            imageView.setImageResource(active ? R.drawable.ic_remove_circle : R.drawable.ic_add_circle);
    }


    public void update() {
        node.setSelected(!node.isSelected());
        textView.setTextColor(node.isSelected() ? ContextCompat.getColor(context, R.color.colorPrimary) : ContextCompat.getColor(context, R.color.gray_444));
        checkBox.setChecked(node.isSelected());
    }
}

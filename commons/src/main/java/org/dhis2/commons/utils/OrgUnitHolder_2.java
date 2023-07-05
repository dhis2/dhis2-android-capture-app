package org.dhis2.commons.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.commons.R;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.Locale;
import java.util.Objects;

/**
 * QUADRAM. Created by ppajuelo on 19/10/2017.
 */

public class OrgUnitHolder_2 extends TreeNode.BaseNodeViewHolder<OrganisationUnit> {

    private final Boolean isMultiSelection;
    private TextView textView;
    private ImageView imageView;
    private CheckBox checkBox;
    private TreeNode node;
    private OrganisationUnit value;
    private final OrganisationUnit selectedOrgUnit;
    private final String orgUnitName;
    private int numberOfSelections;

    public OrgUnitHolder_2(Context context, Boolean isMultiSelection, OrganisationUnit selectedOrgUnit, String orgUnitName) {
        super(context);
        this.isMultiSelection = isMultiSelection;
        this.selectedOrgUnit = selectedOrgUnit;
        this.orgUnitName = orgUnitName;
    }

    @Override
    public View createNodeView(TreeNode node, OrganisationUnit value) {
        this.value = value;
        this.node = node;
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View view = layoutInflater.inflate(R.layout.item_node, null, false);
        textView = view.findViewById(R.id.org_unit_name);
        imageView = view.findViewById(R.id.org_unit_icon);
        textView.setText(value.displayName());
        checkBox = view.findViewById(R.id.checkbox);
        checkBox.setChecked(isMultiSelection && node.isSelectable());

        if (!orgUnitName.equals("")) {
            setSelectedNode();
        }


        imageView.setOnClickListener(v -> {
            if (node.isExpanded())
                node.getViewHolder().getTreeView().collapseNode(node);
            else
                node.getViewHolder().getTreeView().expandNode(node);
        });

        checkBox.setOnClickListener(v -> {
            if (Boolean.FALSE.equals(isMultiSelection)) {
                for (TreeNode treeNode : node.getViewHolder().getTreeView().getSelected())
                    ((OrgUnitHolder_2) treeNode.getViewHolder()).update();
            }
            update();
        });

        if (!node.isSelectable()) {
            setSelectedSizeText();
            checkBox.setVisibility(View.GONE);
            textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.gray_814));
        } else if (Boolean.TRUE.equals(isMultiSelection)) {
            update();
        }


        if (node.getChildren() == null || node.getChildren().isEmpty())
            imageView.setImageResource(R.drawable.ic_circle);

        return view;
    }

    @Override
    public void toggle(boolean active) {
        if (!node.getChildren().isEmpty())
            imageView.setImageResource(active ? R.drawable.ic_remove_circle : R.drawable.ic_add_circle);
    }

    public void update() {
        if (node != null) {
            node.setSelected(!node.isSelected());
            checkBox.setChecked(node.isSelected());
        }
        setSelectedSizeText();
    }

    public void check() {
        if (checkBox != null)
            checkBox.setChecked(true);
    }

    private void setSelectedNode() {

        if (selectedOrgUnit != null && (Objects.equals(selectedOrgUnit.uid(), value.uid()))) {
            getTreeView().deselectAll();
            update();
            setSelectedSizeText();
        }
    }

    private void setSelectedSizeText() {
        numberOfSelections = 0;
        if (node != null) {
            for (TreeNode n : node.getChildren()) {
                if (n.getViewHolder() instanceof OrgUnitHolder_2) {
                    numberOfSelections += n.isSelected() ? 1 : 0;
                    numberOfSelections += ((OrgUnitHolder_2) n.getViewHolder()).numberOfSelections;
                }
            }

            if (numberOfSelections == 0)
                textView.setText(value.displayName());
            else
                textView.setText(String.format(Locale.getDefault(), "%s (%d)", value.displayName(), numberOfSelections));

            if (node.getLevel() > 1 && node.getParent().getViewHolder() instanceof OrgUnitHolder_2) {
                ((OrgUnitHolder_2) node.getParent().getViewHolder()).setSelectedSizeText();
            }
        }
    }
}

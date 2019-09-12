package org.dhis2.usescases.main.program;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.R;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.Locale;

/**
 * QUADRAM. Created by ppajuelo on 19/10/2017.
 */

public class OrgUnitHolder extends TreeNode.BaseNodeViewHolder<OrganisationUnit> {

    private final Boolean isMultiSelection;
    private TextView textView;
    private ImageView imageView;
    private CheckBox checkBox;
    private TreeNode node;
    private OrganisationUnit value;
    private int numberOfSelections;

    public OrgUnitHolder(Context context, Boolean isMultiSelection) {
        super(context);
        this.isMultiSelection = isMultiSelection;
    }

    @Override
    public View createNodeView(TreeNode node, OrganisationUnit value) {
        this.value = value;
        this.node = node;
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View view = layoutInflater.inflate(R.layout.item_node, null, false);
        textView = view.findViewById(R.id.org_unit_name);
        imageView = view.findViewById(R.id.org_unit_icon);
        int textSize = 21 - (value.level());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        textView.setText(value.displayName());
        checkBox = view.findViewById(R.id.checkbox);
        checkBox.setChecked(isMultiSelection & node.isSelectable());

        imageView.setOnClickListener(v -> {
            if (node.isExpanded())
                node.getViewHolder().getTreeView().collapseNode(node);
            else
                node.getViewHolder().getTreeView().expandNode(node);
        });

        checkBox.setOnClickListener(v -> {
            if (!isMultiSelection)
                for (TreeNode treeNode : node.getViewHolder().getTreeView().getSelected())
                    ((OrgUnitHolder) treeNode.getViewHolder()).update();
            update();
        });

        if (!node.isSelectable()) {
            setSelectedSizeText();
            checkBox.setVisibility(View.GONE);
            textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.gray_814));
        } else if (isMultiSelection) {
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
            textView.setTextColor(node.isSelected() ? ContextCompat.getColor(context, R.color.colorPrimary) : ContextCompat.getColor(context, R.color.gray_444));
            checkBox.setChecked(node.isSelected());
        }
        setSelectedSizeText();
    }

    public void check() {
        if (checkBox != null)
            checkBox.setChecked(true);
    }

    public void uncheck() {
        if (checkBox != null)
            checkBox.setChecked(false);
    }

    private void setSelectedSizeText() {
        numberOfSelections = 0;
        if (node != null) {
            for (TreeNode n : node.getChildren()) {
                if (n.getViewHolder() instanceof OrgUnitHolder) {
                    numberOfSelections += n.isSelected() ? 1 : 0;
                    numberOfSelections += ((OrgUnitHolder) n.getViewHolder()).numberOfSelections;
                }
            }

            if (numberOfSelections == 0)
                textView.setText(value.displayName());
            else
                textView.setText(String.format(Locale.getDefault(), "%s (%d)", value.displayName(), numberOfSelections));

            if (node.getLevel() > 1 && node.getParent().getViewHolder() instanceof OrgUnitHolder) {
                ((OrgUnitHolder) node.getParent().getViewHolder()).setSelectedSizeText();
            }
        }
    }
}

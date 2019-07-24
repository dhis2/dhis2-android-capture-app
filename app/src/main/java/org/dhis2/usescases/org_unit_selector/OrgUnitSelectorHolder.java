package org.dhis2.usescases.org_unit_selector;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.databinding.ItemOuTreeBinding;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

class OrgUnitSelectorHolder extends RecyclerView.ViewHolder {
    private final ItemOuTreeBinding binding;

    public OrgUnitSelectorHolder(@NonNull ItemOuTreeBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TreeNode node) {
        binding.ouName.setText(node.getContent().displayName());
        node.setChecked(FilterManager.getInstance().exist(node.getContent()));
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) binding.getRoot().getLayoutParams();
        marginParams.leftMargin = (node.getLevel() - 1) * 40;
        binding.checkBox.setChecked(node.isChecked());
        if (!node.getHasChild()) {
            binding.icon.setImageResource(R.drawable.ic_circle_primary);
        } else {
            if (node.isOpen()) {
                binding.icon.setImageResource(R.drawable.ic_remove_circle_primary);
            } else {
                binding.icon.setImageResource(R.drawable.ic_add_circle);
            }
        }
        binding.checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            FilterManager.getInstance().addIfCan(node.getContent(), b);
            node.setChecked(FilterManager.getInstance().exist(node.getContent()));
        });
    }
}

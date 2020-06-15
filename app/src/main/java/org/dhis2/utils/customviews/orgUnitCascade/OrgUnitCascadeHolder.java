package org.dhis2.utils.customviews.orgUnitCascade;

import android.view.Gravity;
import android.view.Menu;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.OrgUnitCascadeLevelItemBinding;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitCollectionRepository;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;

import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 22/10/2018.
 */

class OrgUnitCascadeHolder extends RecyclerView.ViewHolder {
    private final OrgUnitCascadeLevelItemBinding binding;
    private final OrgUnitCascadeAdapter adapter;
    private final OrgUnitCascadeDialog.OUSelectionType selectionType;
    private String selectedOrgUnit;
    private PopupMenu menu;
    private OrganisationUnitCollectionRepository ouRepository;
    private OrgUnitItem ouItem;

    public OrgUnitCascadeHolder(@NonNull OrgUnitCascadeLevelItemBinding binding, OrgUnitCascadeAdapter adapter, OrgUnitCascadeDialog.OUSelectionType selectionType) {
        super(binding.getRoot());
        this.binding = binding;
        this.adapter = adapter;
        this.selectionType = selectionType;

        binding.clearButton.setOnClickListener(view -> {
            selectedOrgUnit = null;
            setLevelLabel();
            ouItem.setName(null);
            ouItem.setUid(null);
            adapter.setSelectedLevel(
                    getAdapterPosition() + 1,
                    selectedOrgUnit,
                    false);
        });
    }

    private void setMenu(List<Trio<String, String, Boolean>> data) {
        menu = new PopupMenu(binding.levelText.getContext(), binding.levelText, Gravity.BOTTOM);

        for (Trio<String, String, Boolean> ou : data)
            menu.getMenu().add(Menu.NONE, Menu.NONE, data.indexOf(ou), ou.val1());

        menu.setOnMenuItemClickListener(item -> {
            selectedOrgUnit = item.getOrder() < 0 ? null : ouItem.getLevelOrgUnits().get(item.getOrder()).val0();
            binding.levelText.setText(item.getOrder() < 0 ? data.get(0).val1() : data.get(item.getOrder()).val1());
            ouItem.setName(item.getOrder() < 0 ? data.get(0).val1() : data.get(item.getOrder()).val1());
            ouItem.setUid(item.getOrder() < 0 ? data.get(0).val0() : data.get(item.getOrder()).val0());
            adapter.setSelectedLevel(
                    getAdapterPosition() + 1,
                    selectedOrgUnit,
                    selectionType == OrgUnitCascadeDialog.OUSelectionType.SEARCH ?
                            ouRepository.uid(selectedOrgUnit).blockingExists():
                            ouRepository.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).uid(selectedOrgUnit).blockingExists()
                    );
            return false;
        });
    }

    public void bind(OrgUnitItem orgUnitItem, OrganisationUnitCollectionRepository orgUnitRepository) {
        this.ouItem = orgUnitItem;
        this.ouRepository = orgUnitRepository;
        setLevelLabel();

        binding.levelText.setOnClickListener(view -> {
            if (ouItem.getLevel() == 1 || !isEmpty(ouItem.getParentUid()))
                menu.show();
        });

        if ((ouItem.getLevel() == 1 && !ouItem.canCaptureData()) || ouItem.getLevel() > 1 && ouItem.getLevelOrgUnits().size() == 1 && !isEmpty(ouItem.getParentUid()) && !ouItem.canCaptureData()) {
            Trio<String, String, Boolean> selectedOu = ouItem.getLevelOrgUnits().get(0);
            selectedOrgUnit = selectedOu.val0();
            binding.levelText.setText(selectedOu.val1());
            ouItem.setName(selectedOu.val1());
            ouItem.setUid(selectedOu.val0());
            adapter.setSelectedParent(
                    getAdapterPosition() + 1,
                    selectedOrgUnit,
                    selectedOu.val2());
            binding.levelText.setEnabled(false);
            binding.levelText.setClickable(false);
        } else {
            binding.levelText.setEnabled(true);
            binding.levelText.setClickable(true);
            setMenu(ouItem.getLevelOrgUnits());
        }
    }

    private void setLevelLabel() {
        OrganisationUnitLevel orgUnitLevel = ouItem.getOrganisationUnitLevel();
        if (ouItem.getName() != null)
            binding.levelText.setText(ouItem.getName());
        else if (orgUnitLevel != null)
            binding.levelText.setText(orgUnitLevel.displayName());
        else
            binding.levelText.setText(String.format("Level %d", ouItem.getLevel()));
    }
}

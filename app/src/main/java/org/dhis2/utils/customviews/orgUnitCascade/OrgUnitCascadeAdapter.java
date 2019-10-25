package org.dhis2.utils.customviews.orgUnitCascade;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableInt;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.databinding.OrgUnitCascadeLevelItemBinding;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitCollectionRepository;

import java.util.HashMap;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 22/10/2018.
 */

public class OrgUnitCascadeAdapter extends RecyclerView.Adapter<OrgUnitCascadeHolder> {

    private final OrganisationUnitCollectionRepository orgUnitRepository;
    private final OrgUnitCascadeDialog.OUSelectionType selectionType;
    private List<OrgUnitItem> items; //OrgUnit uid, orgUnit name, paretUid, canBe selected
    private ObservableInt level = new ObservableInt(1);
    private HashMap<Integer, String> selectedParent = new HashMap<>();
    private OrgUnitItem selectedOrgUnit;
    private OrgUnitCascadeAdapterInterface orgUnitCascadeAdapterInterface;

    public interface OrgUnitCascadeAdapterInterface {
        void onNewLevelSelected(boolean canBeSelected);
    }

    OrgUnitCascadeAdapter(List<OrgUnitItem> items, String selectedOrgUnit, OrgUnitCascadeAdapterInterface orgUnitCascadeAdapterInterface, OrganisationUnitCollectionRepository organisationUnits, OrgUnitCascadeDialog.OUSelectionType ouSelectionType) {
        this.items = items;
        this.orgUnitCascadeAdapterInterface = orgUnitCascadeAdapterInterface;
        this.orgUnitRepository = organisationUnits;
        this.selectionType = ouSelectionType;

        if (isEmpty(selectedOrgUnit))
            for (int ouLevel = 1; i < items.size(); ouLevel++)
                selectedParent.put(ouLevel, "");
        else {
            OrganisationUnit ou = organisationUnits.uid(selectedOrgUnit).blockingGet();
            String[] uidPath = ou.path().replaceFirst("/", "").split("/");
            for (int ouLevel = 1; ouLevel < uidPath.length+1; ouLevel++) {
                selectedParent.put(ouLevel, uidPath[ouLevel-1]);
                if (ouLevel > 1)
                    items.get(ouLevel-1).setParentUid(uidPath[ouLevel-1]);
                items.get(ouLevel-1).setName(ou.displayNamePath().get(ouLevel-1));
                items.get(ouLevel-1).setUid(uidPath[ouLevel-1]);
            }

        }
    }

    @NonNull
    @Override
    public OrgUnitCascadeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        OrgUnitCascadeLevelItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.org_unit_cascade_level_item, viewGroup, false);
        return new OrgUnitCascadeHolder(binding, this,selectionType);
    }

    @Override
    public void onBindViewHolder(@NonNull OrgUnitCascadeHolder orgUnitCascadeHolder, int position) {
        items.get(position).setParentUid(selectedParent.get(position));
        orgUnitCascadeHolder.bind(items.get(position), orgUnitRepository);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void setSelectedLevel(int level, String selectedUid, Boolean canBeSelected) {
        this.selectedOrgUnit = null;
        selectedParent.put(level, selectedUid);//Set selected orgUnit for level
        reorderSelectedParent(level);
        this.level.set(level);
        if (orgUnitCascadeAdapterInterface != null)
            orgUnitCascadeAdapterInterface.onNewLevelSelected(canBeSelected);
        notifyDataSetChanged();
    }

    public void setSelectedParent(int level, String selectedUid, Boolean canBeSelected) {
        this.selectedOrgUnit = null;
        selectedParent.put(level, selectedUid);//Set selected orgUnit for level
        this.level.set(level);
        /*if (orgUnitCascadeAdapterInterface != null)
            orgUnitCascadeAdapterInterface.onNewLevelSelected(canBeSelected);*/
    }

    public void reorderSelectedParent(int fromLevel) {
        for (int i = fromLevel + 1; i <= items.size(); i++) {
            selectedParent.remove(i); //Remove selected parents for levels higher than the selected one
            items.get(i - 1).setUid(null);
            items.get(i - 1).setName(null);
            items.get(i - 1).setParentUid(null);
        }
    }

    public void setOrgUnit(OrgUnitItem orgUnit, String path) {
        String[] parentUids = path.replaceFirst("/", "").split("/");
        for (int i = 1; i <= parentUids.length; i++)
            setSelectedLevel(i, parentUids[i - 1], true);
        this.selectedOrgUnit = orgUnit;
    }

    @Nullable
    public String getSelectedOrgUnit() {
        String selectedUid = null;
        for (int i = 1; i <= selectedParent.size(); i++) {
            if (!selectedParent.get(i).isEmpty())
                selectedUid = selectedParent.get(i);
        }
        return selectedUid;
    }
}

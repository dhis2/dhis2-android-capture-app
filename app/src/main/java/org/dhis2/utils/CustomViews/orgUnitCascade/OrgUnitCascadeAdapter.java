package org.dhis2.utils.CustomViews.orgUnitCascade;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.tuples.Quartet;
import org.dhis2.data.tuples.Quintet;
import org.dhis2.databinding.OrgUnitCascadeLevelItemBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QUADRAM. Created by ppajuelo on 22/10/2018.
 */

public class OrgUnitCascadeAdapter extends RecyclerView.Adapter<OrgUnitCascadeHolder> {

    private Map<Integer, List<Quartet<String, String, String, Boolean>>> items; //OrgUnit uid, orgUnit name, paretUid, canBe selected
    private ObservableInt level = new ObservableInt(1);
    private HashMap<Integer, String> selectedParent = new HashMap<>();
    private Quintet<String, String, String, Integer, Boolean> selectedOrgUnit;
    private OrgUnitCascadeAdapterInterface orgUnitCascadeAdapterInterface;

    public interface OrgUnitCascadeAdapterInterface {
        void onNewLevelSelected(boolean canBeSelected);
    }

    OrgUnitCascadeAdapter(List<Quintet<String, String, String, Integer, Boolean>> orgUnits, OrgUnitCascadeAdapterInterface orgUnitCascadeAdapterInterface) {
        items = new HashMap<>();
        this.orgUnitCascadeAdapterInterface = orgUnitCascadeAdapterInterface;

        for (Quintet<String, String, String, Integer, Boolean> orgUnit : orgUnits) {
            if (items.get(orgUnit.val3()) == null)
                items.put(orgUnit.val3(), new ArrayList<>());

            items.get(orgUnit.val3()).add(Quartet.create(orgUnit.val0(), orgUnit.val1(), orgUnit.val2(), orgUnit.val4()));
        }

        for (int i = 1; i < items.size(); i++)
            selectedParent.put(i, "");
    }

    @NonNull
    @Override
    public OrgUnitCascadeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        OrgUnitCascadeLevelItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.org_unit_cascade_level_item, viewGroup, false);
        return new OrgUnitCascadeHolder(binding, viewGroup.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull OrgUnitCascadeHolder orgUnitCascadeHolder, int position) {
        orgUnitCascadeHolder.bind(items.get(position + 1),
                position != 0 ? selectedParent.get(position) : "",
                selectedOrgUnit,
                selectedParent.get(position + 1),
                this);
    }

    @Override
    public int getItemCount() {

        int size;

        if (selectedParent.get(level.get()).isEmpty())
            size = level.get();
        else
            size = level.get() + 1 <= items.size() ? level.get() + 1 : items.size();

        if (items.get(level.get() + 1) != null && items.get(level.get() + 1).isEmpty())
            size--;

        return size;
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

    public void reorderSelectedParent(int fromLevel) {
        for (int i = fromLevel + 1; i <= items.size(); i++)
            selectedParent.put(i, ""); //Remove selected parents for levels higher than the selected one
    }

    public void setOrgUnit(Quintet<String, String, String, Integer, Boolean> orgUnit, String path) {
        String[] parentUids = path.replaceFirst("/", "").split("/");
        for (int i = 1; i <= parentUids.length; i++)
            setSelectedLevel(i, parentUids[i - 1], parentUids[i - 1].equals(orgUnit.val0()) ? orgUnit.val4() : false);
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

package org.dhis2.utils.custom_views.orgUnitCascade;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import org.dhis2.R;
import org.dhis2.data.tuples.Quartet;
import org.dhis2.data.tuples.Quintet;
import org.dhis2.databinding.OrgUnitCascadeLevelItemBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 22/10/2018.
 */

class OrgUnitCascadeHolder extends RecyclerView.ViewHolder {
    private final OrgUnitCascadeLevelItemBinding binding;
    private List<Quartet<String, String, String, Boolean>> levelOrgUnit;
    private String selectedUid;
    private PopupMenu menu;
    private Context context;

    public OrgUnitCascadeHolder(@NonNull OrgUnitCascadeLevelItemBinding binding, @NonNull Context context) {
        super(binding.getRoot());
        this.binding = binding;
        this.context = context;
    }

    public void bind(List<Quartet<String, String, String, Boolean>> organisationUnitModels,
                     String parent,
                     Quintet<String, String, String, Integer, Boolean> selectedOrgUnit,
                     String currentUid,
                     OrgUnitCascadeAdapter adapter) {

        this.levelOrgUnit = organisationUnitModels;
        if (selectedOrgUnit != null) {
            this.selectedUid = selectedOrgUnit.val0();
        }
        Collections.sort(levelOrgUnit,
                (Quartet<String, String, String, Boolean> ou1, Quartet<String, String, String, Boolean> ou2) ->
                        ou1.val1().compareTo(ou2.val1()));

        ArrayList<String> data = new ArrayList<>();
        data.add(String.format(context.getString(R.string.org_unit_select_level), getAdapterPosition() + 1));

        String selectedOrgUnitName = null;
        if(!isEmpty(currentUid))
            for(Quartet<String, String, String, Boolean> orgUnit : levelOrgUnit)
                if(orgUnit.val0().equals(currentUid))
                    selectedOrgUnitName = orgUnit.val1();

        if (binding.levelText.getText() == null || binding.levelText.getText().toString().isEmpty() || isEmpty(currentUid))
            binding.levelText.setText(isEmpty(selectedOrgUnitName) ? String.format(context.getString(R.string.org_unit_select_level), getAdapterPosition() + 1) : selectedOrgUnitName);

        for (Quartet<String, String, String, Boolean> trio : levelOrgUnit)
            if (parent.isEmpty() || trio.val2().equals(parent)) //Only if ou is child of parent or is root
                data.add(trio.val1());

        if (data.size() > 1/* && selectedUid == null*/) {
            itemView.setVisibility(View.VISIBLE);
            setMenu(data, adapter);
            binding.levelText.setOnClickListener(view -> menu.show());
        } else if (data.size() <= 1)
            itemView.setVisibility(View.GONE);
    }

    private void setMenu(ArrayList<String> data, OrgUnitCascadeAdapter adapter) {
        menu = new PopupMenu(binding.levelText.getContext(), binding.levelText, Gravity.BOTTOM);

        for (String label : data)
            menu.getMenu().add(Menu.NONE, Menu.NONE, data.indexOf(label), label);

        menu.setOnMenuItemClickListener(item -> {
            for (Quartet<String, String, String, Boolean> trio : levelOrgUnit)
                if (trio.val1().equals(item.getTitle())) {
                    selectedUid = item.getOrder() <= 0 ? "" : trio.val0();
                    binding.levelText.setText(item.getOrder() < 0 ? data.get(0) : data.get(item.getOrder()));
                    adapter.setSelectedLevel(
                            getAdapterPosition() + 1,
                            selectedUid,
                            item.getOrder() <= 0 ? false : trio.val3());
                }
            return false;
        });
    }
}

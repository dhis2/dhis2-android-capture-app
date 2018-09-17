package org.dhis2.utils.CustomViews;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import org.dhis2.R;
import org.dhis2.databinding.DialogOrgunitBinding;
import org.dhis2.usescases.main.program.OrgUnitHolder;
import org.dhis2.utils.OrgUnitUtils;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 21/05/2018.
 */

public class OrgUnitDialog extends DialogFragment {
    DialogOrgunitBinding binding;
    AndroidTreeView treeView;
    static boolean isMultiSelection = false;
    static OrgUnitDialog instace;
    private View.OnClickListener possitiveListener;
    private View.OnClickListener negativeListener;
    private String title;
    private List<OrganisationUnitModel> myOrgs;


    public static OrgUnitDialog newInstace(boolean multiSelection) {
        if (instace == null || instace.isMultiSelection() != multiSelection) {
            isMultiSelection = multiSelection;
            instace = new OrgUnitDialog();
        }
        return instace;
    }

    public OrgUnitDialog setPossitiveListener(View.OnClickListener listener) {
        this.possitiveListener = listener;
        return this;
    }

    public OrgUnitDialog setNegativeListener(View.OnClickListener listener) {
        this.negativeListener = listener;
        return this;
    }

    public OrgUnitDialog setMultiSelection(boolean multiSelection) {
        isMultiSelection = multiSelection;
        return this;
    }

    public OrgUnitDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public OrgUnitDialog setOrgUnits(List<OrganisationUnitModel> orgUnits) {
        this.myOrgs = orgUnits;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_orgunit, container, false);

        binding.setTitleText(title);
        renderTree(myOrgs);
        binding.acceptButton.setOnClickListener(possitiveListener);
        binding.clearButton.setOnClickListener(negativeListener);

        return binding.getRoot();
    }

    public boolean isMultiSelection() {
        return isMultiSelection;
    }

    private void renderTree(@NonNull List<OrganisationUnitModel> myOrgs) {

        binding.treeContainer.removeAllViews();
        treeView = new AndroidTreeView(getContext(), OrgUnitUtils.renderTree(getContext(), myOrgs, false));
        treeView.deselectAll();
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);
        binding.treeContainer.addView(treeView.getView());
        if (myOrgs.size() < 30)
            treeView.expandAll();

        treeView.setDefaultNodeClickListener((node, value) -> {
            for (TreeNode treeNode : node.getViewHolder().getTreeView().getSelected())
                ((OrgUnitHolder) treeNode.getViewHolder()).update();
            ((OrgUnitHolder) node.getViewHolder()).update();
        });
    }

    public String getSelectedOrgUnit() {
        return treeView.getSelected() != null && !treeView.getSelected().isEmpty() ? ((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).uid() : "";
    }

    public String getSelectedOrgUnitName() {
        return treeView.getSelected() != null && !treeView.getSelected().isEmpty() ? ((OrganisationUnitModel) treeView.getSelected().get(0).getValue()).displayName() : "";
    }

    public OrganisationUnitModel getSelectedOrgUnitModel() {
        return ((OrganisationUnitModel) treeView.getSelected().get(0).getValue());
    }
}

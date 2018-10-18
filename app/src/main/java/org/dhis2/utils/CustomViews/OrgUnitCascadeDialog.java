package org.dhis2.utils.CustomViews;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.dhis2.R;
import org.dhis2.databinding.DialogOrgunitBinding;
import org.dhis2.usescases.main.program.OrgUnitHolder;
import org.dhis2.utils.OrgUnitUtils;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 21/05/2018.
 */

public class OrgUnitCascadeDialog extends DialogFragment {
    DialogOrgunitBinding binding;
    AndroidTreeView treeView;
    static boolean isMultiSelection = false;
    static OrgUnitCascadeDialog instace;
    private View.OnClickListener possitiveListener;
    private View.OnClickListener negativeListener;
    private String title;
    private List<OrganisationUnitModel> myOrgs;
    private Context context;


    public static OrgUnitCascadeDialog newInstace(boolean multiSelection) {
        if (instace == null || instace.isMultiSelection() != multiSelection) {
            instace = new OrgUnitCascadeDialog();
            isMultiSelection = multiSelection;
        }
        return instace;
    }

    public OrgUnitCascadeDialog(){
        instace = null;
        isMultiSelection = false;
        possitiveListener = null;
        negativeListener = null;
        title = null;
        myOrgs = null;


    }

    public OrgUnitCascadeDialog setPossitiveListener(View.OnClickListener listener) {
        this.possitiveListener = listener;
        return this;
    }

    public OrgUnitCascadeDialog setNegativeListener(View.OnClickListener listener) {
        this.negativeListener = listener;
        return this;
    }

    public OrgUnitCascadeDialog setMultiSelection(boolean multiSelection) {
        isMultiSelection = multiSelection;
        return this;
    }

    public OrgUnitCascadeDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public OrgUnitCascadeDialog setOrgUnits(List<OrganisationUnitModel> orgUnits) {
        this.myOrgs = orgUnits;
        return this;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_cascade_orgunit, container, false);

        binding.title.setText(title);
        binding.acceptButton.setOnClickListener(possitiveListener);
        binding.clearButton.setOnClickListener(negativeListener);
        renderTree(myOrgs);

        return binding.getRoot();
    }

    public boolean isMultiSelection() {
        return isMultiSelection;
    }

    private void renderTree(@NonNull List<OrganisationUnitModel> myOrgs) {

        binding.treeContainer.removeAllViews();
        treeView = new AndroidTreeView(getContext(), OrgUnitUtils.renderTree(context, myOrgs, isMultiSelection));
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

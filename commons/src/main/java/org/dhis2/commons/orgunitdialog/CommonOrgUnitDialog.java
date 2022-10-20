package org.dhis2.commons.orgunitdialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.dhis2.commons.R;
import org.dhis2.commons.databinding.DialogOrgunitCommonBinding;
import org.dhis2.commons.utils.OrgUnitUtils;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 21/05/2018.
 * SAUDIGITUS. Updated by miguelmacamo on 06/10/2022.
 */

public class CommonOrgUnitDialog extends DialogFragment {
    DialogOrgunitCommonBinding binding;
    AndroidTreeView treeView;
    boolean isMultiSelection = false;
    static CommonOrgUnitDialog instance;
    private View.OnClickListener possitiveListener;
    private View.OnClickListener negativeListener;
    private TreeNode.TreeNodeClickListener nodeClickListener;
    private String title;
    private List<OrganisationUnit> myOrgs;
    private List<OrganisationUnit> mySelectedOrg;
    private OrganisationUnit selectedOrg;
    private Context context;
    private TreeNode treeNode;
    private String programUid;
    private String orgUnitName;

    public static CommonOrgUnitDialog getInstace() {
        if (instance == null) {
            instance = new CommonOrgUnitDialog();
        }
        return instance;
    }

    public CommonOrgUnitDialog() {
        instance = null;
        isMultiSelection = false;
        possitiveListener = null;
        negativeListener = null;
        title = null;
        myOrgs = null;
        mySelectedOrg = null;
        selectedOrg = null;
        orgUnitName = null;
    }

    public CommonOrgUnitDialog setPossitiveListener(View.OnClickListener listener) {
        this.possitiveListener = listener;
        return this;
    }

    public CommonOrgUnitDialog setNegativeListener(View.OnClickListener listener) {
        this.negativeListener = listener;
        return this;
    }

    public CommonOrgUnitDialog setNodeClickListener(TreeNode.TreeNodeClickListener listener) {
        this.nodeClickListener = listener;
        return this;
    }

    public CommonOrgUnitDialog setMultiSelection(boolean multiSelection) {
        isMultiSelection = multiSelection;
        return this;
    }

    public CommonOrgUnitDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public CommonOrgUnitDialog setOrgUnits(List<OrganisationUnit> orgUnits) {
        this.myOrgs = orgUnits;
        return this;
    }

    public CommonOrgUnitDialog setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
        return this;
    }

    public CommonOrgUnitDialog setProgram(String programUid) {
        this.programUid = programUid;
        return this;
    }
    public CommonOrgUnitDialog setOrgUnitName(String orgUnitName) {
        this.orgUnitName = orgUnitName;
        return this;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if(title == null){
            title = getString(R.string.enrollment_org_unit);
        }
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
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_orgunit_common, container, false);

        binding.title.setText(title);
        binding.acceptButton.setOnClickListener(possitiveListener);
        binding.clearButton.setOnClickListener(negativeListener);
        binding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<OrganisationUnit> searchOrgUnits = new ArrayList<>();
                for (OrganisationUnit orgUnit : myOrgs)
                    if (orgUnit.name().toLowerCase().contains(s.toString().toLowerCase()))
                        searchOrgUnits.add(orgUnit);

                renderTree(searchOrgUnits);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //empty
            }
        });

        if (myOrgs != null)
            renderTree(myOrgs);
        setRetainInstance(true);
        return binding.getRoot();
    }

    public boolean isMultiSelection() {
        return isMultiSelection;
    }

    private void renderTree(@NonNull List<OrganisationUnit> myOrgs) {

        binding.treeContainer.removeAllViews();
        treeView = new AndroidTreeView(getContext(), OrgUnitUtils.renderTree_2(context, myOrgs, isMultiSelection, programUid, this.selectedOrg, orgUnitName));
        treeView.deselectAll();
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);
        binding.treeContainer.addView(treeView.getView());
        if (myOrgs.size() < 25)
            treeView.expandAll();

        treeView.setDefaultNodeClickListener(nodeClickListener);
    }

    public String getSelectedOrgUnit() {
        return treeView.getSelected() != null && !treeView.getSelected().isEmpty() ? ((OrganisationUnit) treeView.getSelected().get(0).getValue()).uid() : "";
    }

    public String getSelectedOrgUnitName() {
        return treeView.getSelected() != null && !treeView.getSelected().isEmpty() ? ((OrganisationUnit) treeView.getSelected().get(0).getValue()).displayName() : "";
    }

    public OrganisationUnit getSelectedOrgUnitModel() {
        if (treeView.getSelected().size() == 0)
            return null;
        return ((OrganisationUnit) treeView.getSelected().get(0).getValue());
    }

    public CommonOrgUnitDialog setOrgUnit(OrganisationUnit orgUnit) {
        List<OrganisationUnit> list = new ArrayList<>();
        list.add(orgUnit);
        this.mySelectedOrg = list;
        this.selectedOrg = orgUnit;
        return this;
    }

    public AndroidTreeView getTreeView() {
        return treeView;
    }

    @Override
    public void dismiss() {
        instance = null;
        super.dismiss();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        instance = null;
        super.onCancel(dialog);
    }
}

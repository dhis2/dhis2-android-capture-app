package org.dhis2.utils.customviews;

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

import org.dhis2.R;
import org.dhis2.databinding.DialogOrgunitBinding;
import org.dhis2.utils.OrgUnitUtils;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 21/05/2018.
 */

public class OrgUnitDialog extends DialogFragment {
    DialogOrgunitBinding binding;
    AndroidTreeView treeView;
    boolean isMultiSelection = false;
    static OrgUnitDialog instace;
    private View.OnClickListener possitiveListener;
    private View.OnClickListener negativeListener;
    private TreeNode.TreeNodeClickListener nodeClickListener;
    private String title;
    private List<OrganisationUnit> myOrgs;
    private Context context;
    private TreeNode treeNode;
    private String programUid;

    public static OrgUnitDialog getInstace() {
        if (instace == null) {
            instace = new OrgUnitDialog();
        }
        return instace;
    }

    public OrgUnitDialog() {
        instace = null;
        isMultiSelection = false;
        possitiveListener = null;
        negativeListener = null;
        title = null;
        myOrgs = null;


    }

    public OrgUnitDialog setPossitiveListener(View.OnClickListener listener) {
        this.possitiveListener = listener;
        return this;
    }

    public OrgUnitDialog setNegativeListener(View.OnClickListener listener) {
        this.negativeListener = listener;
        return this;
    }

    public OrgUnitDialog setNodeClickListener(TreeNode.TreeNodeClickListener listener) {
        this.nodeClickListener = listener;
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

    public OrgUnitDialog setOrgUnits(List<OrganisationUnit> orgUnits) {
        this.myOrgs = orgUnits;
        return this;
    }

    public OrgUnitDialog setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
        return this;
    }

    public OrgUnitDialog setProgram(String programUid) {
        this.programUid = programUid;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_orgunit, container, false);

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
        treeView = new AndroidTreeView(getContext(), OrgUnitUtils.renderTree_2(context, myOrgs, isMultiSelection, programUid));
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

    public AndroidTreeView getTreeView() {
        return treeView;
    }

    @Override
    public void dismiss() {
        instace = null;
        super.dismiss();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        instace = null;
        super.onCancel(dialog);
    }
}

package org.dhis2.utils.custom_views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
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
import org.dhis2.usescases.main.program.OrgUnitHolder;
import org.dhis2.utils.OrgUnitUtils;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 21/05/2018.
 */

public class OrgUnitDialog2 extends DialogFragment {
    DialogOrgunitBinding binding;
    private AndroidTreeView treeView;
    private boolean isMultiSelection;
    private static OrgUnitDialog2 instace;
    private View.OnClickListener possitiveListener;
    private View.OnClickListener negativeListener;
    private String title;
    private List<OrganisationUnit> myOrgs;
    private Context context;

    public static OrgUnitDialog2 getInstace() {
        if (instace == null) {
            instace = new OrgUnitDialog2();
        }
        return instace;
    }

    public OrgUnitDialog2() {
        isMultiSelection = false;
        possitiveListener = null;
        negativeListener = null;
        title = null;
        myOrgs = null;


    }

    public OrgUnitDialog2 setPossitiveListener(View.OnClickListener listener) {
        this.possitiveListener = listener;
        return this;
    }

    public OrgUnitDialog2 setNegativeListener(View.OnClickListener listener) {
        this.negativeListener = listener;
        return this;
    }

    public OrgUnitDialog2 setMultiSelection(boolean multiSelection) {
        isMultiSelection = multiSelection;
        return this;
    }

    public OrgUnitDialog2 setTitle(String title) {
        this.title = title;
        return this;
    }

    public OrgUnitDialog2 setOrgUnits(List<OrganisationUnit> orgUnits) {
        this.myOrgs = orgUnits;
        return this;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_orgunit, container, false);

        binding.title.setText(title);
        binding.acceptButton.setOnClickListener(possitiveListener);
        binding.clearButton.setOnClickListener(negativeListener);
        renderTree(myOrgs);
        setRetainInstance(true);
        return binding.getRoot();
    }

    private void renderTree(@NonNull List<OrganisationUnit> myOrgs) {

        binding.treeContainer.removeAllViews();
        treeView = new AndroidTreeView(getContext(), OrgUnitUtils.renderTree2(context, myOrgs, isMultiSelection));
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

    @Override
    public void dismiss() {
        destroyInstance();
        super.dismiss();
    }

    private static void destroyInstance() {
        instace = null;
    }
}

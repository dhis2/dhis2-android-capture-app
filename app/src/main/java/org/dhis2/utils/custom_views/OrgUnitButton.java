package org.dhis2.utils.custom_views;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RelativeLayout;

import org.dhis2.R;
import org.dhis2.usescases.main.program.OrgUnitHolder;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

/**
 * QUADRAM. Created by ppajuelo on 18/04/2018.
 */

public class OrgUnitButton extends RelativeLayout {

    private LayoutInflater inflater;
    private boolean isBgTransparent;
    private CompositeDisposable disposable;
    private ViewDataBinding binding;
    private Button button;

    public OrgUnitButton(Context context) {
        super(context);
        if (!isInEditMode())
            init(context);
        else
            editMode();

    }

    public OrgUnitButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode())
            init(context);
        else
            editMode();

    }

    public OrgUnitButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode())
            init(context);
        else
            editMode();
    }

    private void editMode() {
        LayoutInflater.from(getContext()).inflate(R.layout.form_org_unit, this, false);
    }

    private void init(Context context) {
        inflater = LayoutInflater.from(context);
        disposable = new CompositeDisposable();
        setLayout();

    }

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
    }

    private void setLayout() {
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.form_org_unit, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.form_org_unit, this, true);

        button = findViewById(R.id.button_org_unit);

    }

    public Observable<AndroidTreeView> renderTree(@NonNull List<OrganisationUnitModel> myOrgs) {

        HashMap<Integer, ArrayList<TreeNode>> subLists = new HashMap<>();

        List<OrganisationUnitModel> allOrgs = new ArrayList<>();
        allOrgs.addAll(myOrgs);
        for (OrganisationUnitModel myorg : myOrgs) {
            String[] pathName = myorg.displayNamePath().split("/");
            String[] pathUid = myorg.path().split("/");
            for (int i = myorg.level() - 1; i > 0; i--) {
                OrganisationUnitModel orgToAdd = OrganisationUnitModel.builder()
                        .uid(pathUid[i])
                        .level(i)
                        .parent(pathUid[i - 1])
                        .name(pathName[i])
                        .displayName(pathName[i])
                        .displayShortName(pathName[i])
                        .build();
                if (!allOrgs.contains(orgToAdd))
                    allOrgs.add(orgToAdd);
            }
        }

        Collections.sort(myOrgs, (org1, org2) -> org2.level().compareTo(org1.level()));

        for (int i = 0; i < myOrgs.get(0).level(); i++) {
            subLists.put(i + 1, new ArrayList<>());
        }

        //Separamos las orunits en listas por nivel
        for (OrganisationUnitModel orgs : allOrgs) {
            ArrayList<TreeNode> sublist = subLists.get(orgs.level());
            TreeNode treeNode = new TreeNode(orgs).setViewHolder(new OrgUnitHolder(getContext(),true));
            treeNode.setSelectable(orgs.path() != null);
            sublist.add(treeNode);
            subLists.put(orgs.level(), sublist);
        }

        TreeNode root = TreeNode.root();
        root.addChildren(subLists.get(1));

        for (int level = myOrgs.get(0).level(); level > 1; level--) {
            for (TreeNode treeNode : subLists.get(level - 1)) {
                for (TreeNode treeNodeLevel : subLists.get(level)) {
                    if (((OrganisationUnitModel) treeNodeLevel.getValue()).parent().equals(((OrganisationUnitModel) treeNode.getValue()).uid()))
                        treeNode.addChild(treeNodeLevel);
                }
            }
        }

        return Observable.just(configureTreeView(root));

    }

    private AndroidTreeView configureTreeView(TreeNode root) {
        AndroidTreeView treeView = new AndroidTreeView(getContext(), root);

        treeView.setDefaultContainerStyle(R.style.TreeNodeStyle, false);
        treeView.setSelectionModeEnabled(true);

        treeView.expandAll();

        treeView.setDefaultNodeClickListener((node, value) -> {
            if (treeView.getSelected().size() == 1 && !node.isSelected()) {
                ((OrgUnitHolder) node.getViewHolder()).update();
                button.setText(String.format("(%s) Org Unit", treeView.getSelected().size()));
            } else if (treeView.getSelected().size() > 1) {
                ((OrgUnitHolder) node.getViewHolder()).update();
                button.setText(String.format("(%s) Org Unit", treeView.getSelected().size()));
            }
        });

        button.setText(String.format("(%s) Org Unit", treeView.getSelected().size()));
        return treeView;
    }

}

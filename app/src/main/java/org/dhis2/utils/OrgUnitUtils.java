package org.dhis2.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import org.dhis2.usescases.main.program.OrgUnitHolder;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * QUADRAM. Created by ppajuelo on 24/05/2018.
 */

public class OrgUnitUtils {

    public static TreeNode renderTree(Context context, @NonNull List<OrganisationUnitModel> myOrgs, Boolean isMultiSelection) {

        HashMap<Integer, ArrayList<TreeNode>> subLists = new HashMap<>();

        List<OrganisationUnitModel> allOrgs = new ArrayList<>();
        ArrayList<String> myOrgUnitUids = new ArrayList<>();
        for (OrganisationUnitModel myorg : myOrgs) {
            myOrgUnitUids.add(myorg.uid());
            String[] pathName = myorg.displayNamePath().split("/");
            String[] pathUid = myorg.path().split("/");
            for (int i = myorg.level(); i > 0; i--) {
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
            TreeNode treeNode = new TreeNode(orgs).setViewHolder(new OrgUnitHolder(context, isMultiSelection));
            treeNode.setSelectable(myOrgUnitUids.contains(orgs.uid()));
            sublist.add(treeNode);
            subLists.put(orgs.level(), sublist);
        }


        SortedSet<Integer> keys = new TreeSet<>(subLists.keySet());

        for (int level = keys.last(); level > 1; level--) {
            for (TreeNode treeNode : subLists.get(level - 1)) {
                for (TreeNode childTreeNode : subLists.get(level)) {
                    if (((OrganisationUnitModel) childTreeNode.getValue()).parent().equals(((OrganisationUnitModel) treeNode.getValue()).uid()))
                        treeNode.addChild(childTreeNode);
                }

            }
        }

        TreeNode root = TreeNode.root();
        root.addChildren(subLists.get(1));

        return root;
    }

}

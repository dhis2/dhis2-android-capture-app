package org.dhis2.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.usescases.main.program.OrgUnitHolder;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 24/05/2018.
 */

public class OrgUnitUtils {

    public static TreeNode renderTree(Context context, @NonNull List<OrganisationUnitModel> myOrgs, Boolean isMultiSelection) {

        HashMap<Integer, ArrayList<TreeNode>> subLists = new HashMap<>();

        List<OrganisationUnitModel> allOrgs = new ArrayList<>();
        ArrayList<String> myOrgUnitUids = new ArrayList<>();
        if (myOrgs == null) {
            myOrgs = new ArrayList<>();
        }
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

        if (!myOrgs.isEmpty() && myOrgs.get(0) != null && myOrgs.get(0).level() != null) {
            for (int i = 0; i < myOrgs.get(0).level(); i++) {
                subLists.put(i + 1, new ArrayList<>());
            }
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

        try {
            if(!keys.isEmpty()) {
                for (int level = keys.last(); level > 1; level--) {
                    for (TreeNode treeNode : subLists.get(level - 1)) {
                        for (TreeNode childTreeNode : subLists.get(level)) {
                            if (((OrganisationUnitModel) childTreeNode.getValue()).parent().equals(((OrganisationUnitModel) treeNode.getValue()).uid()))
                                treeNode.addChild(childTreeNode);
                        }

                    }
                }
            }
        } catch (NoSuchElementException e) { //It seems keys.last() can result in a null
            Timber.e(e);
        }

        TreeNode root = TreeNode.root();
        root.addChildren(subLists.get(1));

        return root;
    }

    public static List<ParentChildModel<OrganisationUnitModel>> renderTree(@NonNull List<OrganisationUnitModel> myOrgs) {

        HashMap<Integer, ArrayList<OrganisationUnitModel>> orgUnitsByLevel = new HashMap<>();

        List<ParentChildModel<OrganisationUnitModel>> allOrgs = new ArrayList<>();
        ArrayList<String> myOrgUnitUids = new ArrayList<>();

        int minLevel = -1;
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

                ParentChildModel<OrganisationUnitModel> orgUnitParent =
                        ParentChildModel.create(orgToAdd, new ArrayList<>(), myOrgUnitUids.contains(orgToAdd.uid()));

                if (!allOrgs.contains(orgUnitParent)) {
                    allOrgs.add(orgUnitParent);
                }

                if (orgUnitsByLevel.get(i) == null)
                    orgUnitsByLevel.put(i, new ArrayList<>());
                orgUnitsByLevel.get(i).add(myorg);

                if (minLevel == -1 || i < minLevel)
                    minLevel = i;
            }
        }

        List<ParentChildModel<OrganisationUnitModel>> minLevelList = new ArrayList<>();

        for (ParentChildModel<OrganisationUnitModel> parentModel : allOrgs) {
            for (ParentChildModel<OrganisationUnitModel> childModel : allOrgs)
                if (childModel.parent().parent().equals(parentModel.parent().uid()))
                    parentModel.addItem(childModel);
            if (parentModel.parent().level() == minLevel)
                minLevelList.add(parentModel);
        }

        return minLevelList;

    }

}

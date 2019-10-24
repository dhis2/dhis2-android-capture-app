package org.dhis2.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.utils.customviews.OrgUnitHolder_2;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 24/05/2018.
 */

public class OrgUnitUtils {

    public static TreeNode renderTree_2(Context context, @NonNull List<OrganisationUnit> myOrgs, Boolean isMultiSelection, String programId) {

        HashMap<Integer, ArrayList<TreeNode>> subLists = new HashMap<>();
        Map<String, OrganisationUnit> myOrgUnitMap = new HashMap<>();
        for (OrganisationUnit organisationUnit : myOrgs)
            myOrgUnitMap.put(organisationUnit.uid(), organisationUnit);

        List<OrganisationUnit> allOrgs = new ArrayList<>();
        ArrayList<String> myOrgUnitUids = new ArrayList<>();

        for (OrganisationUnit myorg : myOrgs) {
            myOrgUnitUids.add(myorg.uid());
            String[] pathName = myorg.displayNamePath().split("/");
            String[] pathUid = myorg.path().split("/");
            int count = 0;
            for (int i = 0; i < myorg.displayName().length(); i++) {
                if (myorg.displayName().charAt(i) == '/')
                    count++;
            }

            if (myorg.displayName().contains("/"))
                pathName[(pathName.length - 1) - count] = myorg.displayName();

            for (int i = myorg.level(); i > 0; i--) {
                OrganisationUnit orgToAdd = OrganisationUnit.builder()
                        .uid(pathUid[i])
                        .openingDate(myOrgUnitMap.get(pathUid[i]) != null ? myOrgUnitMap.get(pathUid[i]).openingDate() : null)
                        .closedDate(myOrgUnitMap.get(pathUid[i]) != null ? myOrgUnitMap.get(pathUid[i]).closedDate() : null)
                        .level(i)
//                        .parent(pathUid[i - 1])
                        .path(pathUid[i - 1])
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
        for (OrganisationUnit orgs : allOrgs) {
            ArrayList<TreeNode> sublist = subLists.get(orgs.level());
            TreeNode treeNode = new TreeNode(orgs).setViewHolder(new OrgUnitHolder_2(context, isMultiSelection));
            treeNode.setSelectable(myOrgUnitUids.contains(orgs.uid()));
            sublist.add(treeNode);
            Collections.sort(sublist, (org1, org2) -> ((OrganisationUnit) org1.getValue()).displayName().compareTo(((OrganisationUnit) org2.getValue()).displayName()));
            subLists.put(orgs.level(), sublist);
        }


        SortedSet<Integer> keys = new TreeSet<>(subLists.keySet());

        try {
            if (!keys.isEmpty()) {
                for (int level = keys.last(); level > 1; level--) {
                    for (TreeNode treeNode : subLists.get(level - 1)) {
                        for (TreeNode childTreeNode : subLists.get(level)) {
                            if (((OrganisationUnit) childTreeNode.getValue()).path().equals(((OrganisationUnit) treeNode.getValue()).uid()))
                                treeNode.addChild(childTreeNode);
                        }

                    }
                }
            }
        } catch (NoSuchElementException e) { //It seems keys.last() can result in a null
            Timber.e(e);
        }

        TreeNode root = TreeNode.root();
        if (subLists.size() > 0 && subLists.get(1) != null) {
            root.addChildren(subLists.get(1));
        }

        return root;
    }

    public static List<TreeNode> createNode_2(Context context, List<OrganisationUnit> orgUnits, boolean isMultiSelection) {
        List<TreeNode> levelNode = new ArrayList<>();
        for (OrganisationUnit org : orgUnits) {
            TreeNode treeNode = new TreeNode(org).setViewHolder(new OrgUnitHolder_2(context, isMultiSelection));
            treeNode.setSelectable(true);
            levelNode.add(treeNode);
        }

        return levelNode;
    }
}

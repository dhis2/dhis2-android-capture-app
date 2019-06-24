package org.dhis2.utils

import android.content.Context

import com.unnamed.b.atv.model.TreeNode

import org.dhis2.usescases.main.program.OrgUnitHolder
import org.dhis2.usescases.main.program.OrgUnitHolder_2
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.NoSuchElementException
import java.util.SortedSet
import java.util.TreeSet
import timber.log.Timber

/**
 * QUADRAM. Created by ppajuelo on 24/05/2018.
 */

object OrgUnitUtils {

    fun renderTree(context: Context, myOrgs: List<OrganisationUnitModel>, isMultiSelection: Boolean?): TreeNode {
        var myOrgs = myOrgs

        val subLists = LinkedHashMap<Int, ArrayList<TreeNode>>()
        val myOrgUnitMap = LinkedHashMap<String, OrganisationUnitModel>()
        for (organisationUnit in myOrgs)
            myOrgUnitMap[organisationUnit.uid()!!] = organisationUnit

        val allOrgs = ArrayList<OrganisationUnitModel>()
        val myOrgUnitUids = ArrayList<String>()
        myOrgs = ArrayList()
        for (myorg in myOrgs) {
            myorg.uid()?.let {
                myOrgUnitUids.add(it)
            }
            val pathName = myorg.displayNamePath()!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val pathUid = myorg.path()!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            myorg.level()?.let {
                for (i in it downTo 1) {
                    val orgToAdd = OrganisationUnitModel.builder()
                            .uid(pathUid[i])
                            .openingDate(if (myOrgUnitMap[pathUid[i]] != null) myOrgUnitMap[pathUid[i]]!!.openingDate() else null)
                            .closedDate(if (myOrgUnitMap[pathUid[i]] != null) myOrgUnitMap[pathUid[i]]!!.closedDate() else null)
                            .level(i)
                            .parent(pathUid[i - 1])
                            .name(pathName[i])
                            .displayName(pathName[i])
                            .displayShortName(pathName[i])
                            .build()
                    if (!allOrgs.contains(orgToAdd))
                        allOrgs.add(orgToAdd)
                }
            }
        }
        myOrgs.sortedWith(Comparator<OrganisationUnitModel> { o1, o2 -> o2?.level()!!.compareTo(o1?.level()!!) })

        if (myOrgs.isNotEmpty() && myOrgs[0].level() != null) {
            myOrgs[0].level()?.let {
                for (i in 0 until it) {
                    subLists[i + 1] = ArrayList()
                }
            }
        }

        //Separamos las orunits en listas por nivel
        for (orgs in allOrgs) {
            val sublist = subLists[orgs.level()]
            val treeNode = TreeNode(orgs).setViewHolder(OrgUnitHolder(context, isMultiSelection))
            treeNode.isSelectable = myOrgUnitUids.contains(orgs.uid())
            sublist!!.add(treeNode)
            sublist.sortWith(Comparator { org1, org2 -> (org1.value as OrganisationUnitModel).displayName()!!.compareTo((org2.value as OrganisationUnitModel).displayName()!!) })
            orgs.level()?.let {
                subLists[it] = sublist
            }

        }


        val keys = TreeSet(subLists.keys)

        try {
            if (!keys.isEmpty()) {
                for (level in keys.last() downTo 2) {
                    for (treeNode in subLists[level - 1]!!) {
                        for (childTreeNode in subLists[level]!!) {
                            if ((childTreeNode.value as OrganisationUnitModel).parent() == (treeNode.value as OrganisationUnitModel).uid())
                                treeNode.addChild(childTreeNode)
                        }

                    }
                }
            }
        } catch (e: NoSuchElementException) { //It seems keys.last() can result in a null
            Timber.e(e)
        }

        val root = TreeNode.root()
        if (subLists.size > 0 && subLists[1] != null) {
            root.addChildren(subLists[1]!!)
        }

        return root
    }

    fun renderTree_2(context: Context, myOrgs: List<OrganisationUnit>, isMultiSelection: Boolean?): TreeNode {

        val subLists = LinkedHashMap<Int, ArrayList<TreeNode>>()
        val myOrgUnitMap = HashMap<String, OrganisationUnit>()
        for (organisationUnit in myOrgs)
            myOrgUnitMap[organisationUnit.uid()] = organisationUnit

        val allOrgs = ArrayList<OrganisationUnit>()
        val myOrgUnitUids = ArrayList<String>()

        for (myorg in myOrgs) {
            myOrgUnitUids.add(myorg.uid())
            val pathName = myorg.displayNamePath()!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val pathUid = myorg.path()!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            myorg.level()?.let {
                for (i in it downTo 1) {
                val orgToAdd = OrganisationUnit.builder()
                        .uid(pathUid[i])
                        .openingDate(if (myOrgUnitMap[pathUid[i]] != null) myOrgUnitMap[pathUid[i]]!!.openingDate() else null)
                        .closedDate(if (myOrgUnitMap[pathUid[i]] != null) myOrgUnitMap[pathUid[i]]!!.closedDate() else null)
                        .level(i)
                        //                        .parent(pathUid[i - 1])
                        .path(pathUid[i - 1])
                        .name(pathName[i])
                        .displayName(pathName[i])
                        .displayShortName(pathName[i])
                        .build()
                if (!allOrgs.contains(orgToAdd))
                    allOrgs.add(orgToAdd)
            }
            }
        }
        myOrgs.sortedWith(Comparator { org1, org2 -> org2.level()!!.compareTo(org1.level()!!) })

        if (myOrgs.isNotEmpty() && myOrgs[0].level() != null) {
            myOrgs[0].level()?.let {
                for (i in 0 until it) {
                    subLists[i + 1] = ArrayList()
                }
            }
        }

        //Separamos las orunits en listas por nivel
        for (orgs in allOrgs) {
            val sublist = subLists[orgs.level()]
            val treeNode = TreeNode(orgs).setViewHolder(OrgUnitHolder_2(context, isMultiSelection))
            treeNode.isSelectable = myOrgUnitUids.contains(orgs.uid())
            sublist!!.add(treeNode)
            sublist.sortWith(Comparator { org1, org2 -> (org1.value as OrganisationUnit).displayName()!!.compareTo((org2.value as OrganisationUnit).displayName()!!) })
            orgs.level()?.let {
                subLists[it] = sublist
            }
        }


        val keys = TreeSet(subLists.keys)

        try {
            if (!keys.isEmpty()) {
                for (level in keys.last() downTo 2) {
                    for (treeNode in subLists[level - 1]!!) {
                        for (childTreeNode in subLists[level]!!) {
                            if ((childTreeNode.value as OrganisationUnit).path() == (treeNode.value as OrganisationUnit).uid())
                                treeNode.addChild(childTreeNode)
                        }

                    }
                }
            }
        } catch (e: NoSuchElementException) { //It seems keys.last() can result in a null
            Timber.e(e)
        }

        val root = TreeNode.root()
        if (subLists.size > 0 && subLists[1] != null) {
            root.addChildren(subLists[1]!!)
        }

        return root
    }


    fun createNode(context: Context, orgUnits: List<OrganisationUnitModel>, isMultiSelection: Boolean): List<TreeNode> {
        val levelNode = ArrayList<TreeNode>()
        for (org in orgUnits) {
            val treeNode = TreeNode(org).setViewHolder(OrgUnitHolder(context, isMultiSelection))
            treeNode.isSelectable = true
            levelNode.add(treeNode)
        }

        return levelNode
    }

    fun createNode_2(context: Context, orgUnits: List<OrganisationUnit>, isMultiSelection: Boolean): List<TreeNode> {
        val levelNode = ArrayList<TreeNode>()
        for (org in orgUnits) {
            val treeNode = TreeNode(org).setViewHolder(OrgUnitHolder_2(context, isMultiSelection))
            treeNode.isSelectable = true
            levelNode.add(treeNode)
        }

        return levelNode
    }
}

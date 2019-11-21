/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist
    .RapidFloatingActionContentLabelList
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem
import com.wangjie.rapidfloatingactionbutton.util.RFABTextUtil
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.tuples.Pair
import org.dhis2.data.tuples.Trio
import org.dhis2.databinding.FragmentRelationshipsBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.Constants
import org.dhis2.utils.OnDialogClickListener
import org.hisp.dhis.android.core.relationship.RelationshipType

class RelationshipFragment : FragmentGlobalAbstract(), RelationshipView {

    @Inject
    lateinit var presenter: RelationshipPresenter

    private lateinit var binding: FragmentRelationshipsBinding
    private lateinit var relationshipAdapter: RelationshipAdapter
    private lateinit var rfaHelper: RapidFloatingActionHelper
    private lateinit var relationshipType: RelationshipType

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = context as TeiDashboardMobileActivity
        if ((context.applicationContext as App).dashboardComponent() != null) {
            (context.applicationContext as App)
                .dashboardComponent()!!
                .plus(RelationshipModule(activity.programUid, activity.teiUid, this))
                .inject(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_relationships, container, false
        )
        relationshipAdapter = RelationshipAdapter(presenter)
        binding.relationshipRecycler.adapter = relationshipAdapter
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
    }

    override fun onPause() {
        presenter.onDettach()
        super.onPause()
    }

    override fun setRelationships(relationships: List<RelationshipViewModel>) {
        if (relationshipAdapter != null) {
            relationshipAdapter.addItems(relationships)
        }
        if (relationships != null && relationships.isNotEmpty()) {
            binding.emptyRelationships.visibility = View.GONE
        } else {
            binding.relationshipRecycler.visibility = View.VISIBLE
        }
    }

    override fun setRelationshipTypes(
        relationshipTypes: List<Trio<RelationshipType, String, Int>>
    ) {
        initFab(relationshipTypes)
    }

    override fun goToAddRelationship(teiUid: String, teiTypeToAdd: String) {
        val intent = Intent(context, SearchTEActivity::class.java)
        val extras = Bundle()
        extras.run {
            putBoolean("FROM_RELATIONSHIP", true)
            putString("FROM_RELATIONSHIP_TEI", teiUid)
            putString("TRACKED_ENTITY_UID", teiTypeToAdd)
            putString("PROGRAM_UID", null)
        }

        intent.putExtras(extras)
        (activity as TeiDashboardMobileActivity).toRelationships()
        this.startActivityForResult(intent, Constants.REQ_ADD_RELATIONSHIP)
    }

    override fun goToTeiDashboard(teiUid: String) {
        val intent = Intent(context, TeiDashboardMobileActivity::class.java)
        val bundle = Bundle()

        bundle.run {
            putString("TEI_UID", teiUid)
            putString("PROGRAM_UID", null)
        }

        intent.putExtras(bundle)
        abstractActivity.startActivity(intent)
    }

    override fun showDialogRelationshipWithoutEnrollment(displayName: String) {
        showInfoDialog(
            String.format(
                context!!.getString(R.string.resource_not_found),
                displayName
            ),
            context!!.getString(R.string.relationship_without_enrollment),
            context!!.getString(R.string.ok),
            context!!.getString(R.string.no),
            object : OnDialogClickListener {
                override fun onPossitiveClick(alertDialog: AlertDialog) {}
                override fun onNegativeClick(alertDialog: AlertDialog) {}
            }
        ).show()
    }

    override fun showDialogRelationshipNotFoundMessage(displayName: String) {
        showInfoDialog(
            String.format(
                context!!.getString(R.string.resource_not_found),
                displayName
            ),
            context!!.getString(R.string.relationship_not_found_message),
            context!!.getString(R.string.yes),
            context!!.getString(R.string.no),
            object : OnDialogClickListener {
                override fun onPossitiveClick(alertDialog: AlertDialog) {
                    back()
                }

                override fun onNegativeClick(alertDialog: AlertDialog) {}
            }
        ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((
            requestCode == Constants.REQ_ADD_RELATIONSHIP &&
                resultCode == RESULT_OK
            ) && data != null
        ) {
            presenter.addRelationship(
                data.getStringExtra("TEI_A_UID"),
                relationshipType.uid()
            )
        }
    }

    private fun initFab(relationshipTypes: List<Trio<RelationshipType, String, Int>>) {
        val rfaContent = RapidFloatingActionContentLabelList(abstracContext)
        rfaContent.setOnRapidFloatingActionContentLabelListListener(
            object :
                RapidFloatingActionContentLabelList
                .OnRapidFloatingActionContentLabelListListener<Pair<RelationshipType, String>> {
                override fun onRFACItemLabelClick(
                    position: Int,
                    item: RFACLabelItem<Pair<RelationshipType, String>>?
                ) {
                    val pair = item!!.wrapper
                    goToRelationShip(pair.val0(), pair.val1())
                }

                override fun onRFACItemIconClick(
                    position: Int,
                    item: RFACLabelItem<Pair<RelationshipType, String>>?
                ) {
                    val pair = item!!.wrapper
                    goToRelationShip(pair.val0(), pair.val1())
                }
            })

        val items =
            mutableListOf<RFACLabelItem<Pair<RelationshipType, String>>>()

        relationshipTypes.forEach {
            val relationshipType = it.val0()
            val resource = it.val2()

            items.add(
                RFACLabelItem<Pair<RelationshipType, String>>()
                    .setLabel(relationshipType!!.displayName())
                    .setResId(resource!!.toInt())
                    .setLabelTextBold(true)
                    .setLabelBackgroundDrawable(
                        AppCompatResources.getDrawable(
                            abstracContext,
                            R.drawable.bg_chip
                        )
                    )
                    .setIconNormalColor(
                        ColorUtils.getPrimaryColor(
                            abstracContext,
                            ColorUtils.ColorType.PRIMARY_DARK
                        )
                    )
                    .setWrapper(Pair.create(relationshipType!!, it.val1()!!))
            )
        }

        if (items.isNotEmpty()) {
            rfaContent.setItems(items.toList())
                .setIconShadowRadius(RFABTextUtil.dip2px(abstracContext, 5F))
                .setIconShadowColor(0xff888888.toInt())
                .setIconShadowDy(RFABTextUtil.dip2px(abstracContext, 5F))
                .setIconShadowColor(0xff888888.toInt())

            rfaHelper = RapidFloatingActionHelper(
                abstracContext,
                binding.rfabLayout,
                binding.rfab,
                rfaContent
            ).build()
        }
    }

    private fun goToRelationShip(
        relationshipTypeModel: RelationshipType,
        teiTypeUid: String
    ) {
        rfaHelper.toggleContent()
        relationshipType = relationshipTypeModel
        presenter.goToAddRelationship(teiTypeUid)
    }
}

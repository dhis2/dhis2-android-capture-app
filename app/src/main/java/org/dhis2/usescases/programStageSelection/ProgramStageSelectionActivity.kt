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

package org.dhis2.usescases.programStageSelection

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.ActivityProgramStageSelectionBinding
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.utils.Constants
import org.dhis2.utils.Constants.ENROLLMENT_UID
import org.dhis2.utils.Constants.EVENT_CREATION_TYPE
import org.dhis2.utils.Constants.EVENT_PERIOD_TYPE
import org.dhis2.utils.Constants.EVENT_REPEATABLE
import org.dhis2.utils.Constants.EVENT_SCHEDULE_INTERVAL
import org.dhis2.utils.Constants.ORG_UNIT
import org.dhis2.utils.Constants.PROGRAM_UID
import org.dhis2.utils.Constants.TRACKED_ENTITY_INSTANCE
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage

/**
 * QUADRAM. Created by ppajuelo on 31/10/2017.
 */

class ProgramStageSelectionActivity : ActivityGlobalAbstract(), ProgramStageSelectionView {

    @Inject
    lateinit var presenter: ProgramStageSelectionPresenter

    lateinit var binding: ActivityProgramStageSelectionBinding

    lateinit var adapter: ProgramStageSelectionAdapter
    private lateinit var enrollmentId: String
    private lateinit var programId: String

    public override fun onCreate(savedInstanceState: Bundle?) {
        programId = intent.getStringExtra(PROGRAM_UID)
        enrollmentId = intent.getStringExtra(ENROLLMENT_UID)
        val eventCreationType = intent.getStringExtra(EVENT_CREATION_TYPE)
        (applicationContext as App).userComponent()?.plus(
            ProgramStageSelectionModule(
                this,
                programId,
                enrollmentId,
                eventCreationType
            )
        )?.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_program_stage_selection)
        binding.presenter = presenter
        adapter = ProgramStageSelectionAdapter(presenter)
        binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val orientation = Resources.getSystem().configuration.orientation
        presenter.getProgramStages(programId, enrollmentId) // TODO: enrollment / event path
        val columnCount = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 3 else 2
        binding.recyclerView.layoutManager = GridLayoutManager(this, columnCount)
    }

    override fun onPause() {
        presenter.onDetach()
        super.onPause()
    }

    override fun setData(programStages: List<ProgramStage>) {
        if (programStages.isNotEmpty()) {
            adapter.setProgramStages(programStages)
            adapter.notifyDataSetChanged()
        } else {
            // if there are no program stages to select, the event cannot be added
            displayMessage(getString(R.string.program_not_allow_events))
            finish()
        }
    }

    override fun setResult(programStageUid: String, repeatable: Boolean?, periodType: PeriodType?) {
        Intent(this, EventInitialActivity::class.java).run {
            addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            putExtras(
                Bundle().apply {
                    putString(
                        TRACKED_ENTITY_INSTANCE,
                        intent.getStringExtra(TRACKED_ENTITY_INSTANCE)
                    )
                    putString(PROGRAM_UID, programId)
                    putString(ORG_UNIT, intent.getStringExtra(ORG_UNIT))
                    putString(ENROLLMENT_UID, enrollmentId)
                    putString(EVENT_CREATION_TYPE, intent.getStringExtra(EVENT_CREATION_TYPE))
                    putBoolean(EVENT_REPEATABLE, repeatable ?: false)
                    putSerializable(EVENT_PERIOD_TYPE, periodType)
                    putString(Constants.PROGRAM_STAGE_UID, programStageUid)
                    putInt(EVENT_SCHEDULE_INTERVAL, presenter.getStandardInterval(programStageUid))
                }
            )
            startActivity(this)
        }
        finish()
    }
}

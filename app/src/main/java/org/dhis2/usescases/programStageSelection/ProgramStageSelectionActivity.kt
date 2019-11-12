package org.dhis2.usescases.programStageSelection

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle

import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.ActivityProgramStageSelectionBinding
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.utils.Constants
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage

import javax.inject.Inject
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager

import org.dhis2.utils.Constants.ENROLLMENT_UID
import org.dhis2.utils.Constants.EVENT_CREATION_TYPE
import org.dhis2.utils.Constants.EVENT_PERIOD_TYPE
import org.dhis2.utils.Constants.EVENT_REPEATABLE
import org.dhis2.utils.Constants.EVENT_SCHEDULE_INTERVAL
import org.dhis2.utils.Constants.ORG_UNIT
import org.dhis2.utils.Constants.PROGRAM_UID
import org.dhis2.utils.Constants.TRACKED_ENTITY_INSTANCE


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
        presenter.getProgramStages(programId, enrollmentId) //TODO: enrollment / event path
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
        val intent = Intent(this, EventInitialActivity::class.java)
        val bundle = Bundle()
        bundle.putString(PROGRAM_UID, getIntent().getStringExtra(PROGRAM_UID))
        bundle.putString(
            TRACKED_ENTITY_INSTANCE,
            getIntent().getStringExtra(TRACKED_ENTITY_INSTANCE)
        )
        bundle.putString(ORG_UNIT, getIntent().getStringExtra(ORG_UNIT))
        bundle.putString(ENROLLMENT_UID, getIntent().getStringExtra(ENROLLMENT_UID))
        bundle.putString(EVENT_CREATION_TYPE, getIntent().getStringExtra(EVENT_CREATION_TYPE))
        bundle.putBoolean(EVENT_REPEATABLE, repeatable ?: false)
        bundle.putSerializable(EVENT_PERIOD_TYPE, periodType)
        bundle.putString(Constants.PROGRAM_STAGE_UID, programStageUid)
        bundle.putInt(EVENT_SCHEDULE_INTERVAL, presenter.getStandardInterval(programStageUid))
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }
}

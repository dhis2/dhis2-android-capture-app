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

package org.dhis2.usescases.programStageSelection

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import org.dhis2.App
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.databinding.ActivityProgramStageSelectionBinding
import org.dhis2.form.model.EventMode
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.hisp.dhis.android.core.period.PeriodType
import javax.inject.Inject

class ProgramStageSelectionActivity : ActivityGlobalAbstract(), ProgramStageSelectionView {
    private lateinit var binding: ActivityProgramStageSelectionBinding

    @Inject
    lateinit var presenter: ProgramStageSelectionPresenter

    private lateinit var adapter: ProgramStageSelectionAdapter

    public override fun onCreate(savedInstanceState: Bundle?) {
        initInjector()
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_program_stage_selection)

        binding.presenter = presenter
        adapter = ProgramStageSelectionAdapter { programStage ->
            presenter.onProgramStageClick(programStage)
        }
        binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val orientation = Resources.getSystem().configuration.orientation
        val columnCount = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 3 else 2
        binding.recyclerView.layoutManager = GridLayoutManager(this, columnCount)
        presenter.programStages()
    }

    override fun onPause() {
        presenter.onDettach()
        super.onPause()
    }

    override fun setData(programStages: List<ProgramStageData>) {
        if (programStages.isNotEmpty()) {
            adapter.submitList(programStages)
        } else {
            displayMessage(getString(R.string.program_not_allow_events))
            finish()
        }
    }

    override fun setResult(programStageUid: String, repeatable: Boolean, periodType: PeriodType?) {
        val eventCreationType = intent.getStringExtra(Constants.EVENT_CREATION_TYPE)
        val programUid = intent.getStringExtra(Constants.PROGRAM_UID)
        val enrollmentUid = intent.getStringExtra(Constants.ENROLLMENT_UID)

        when (eventCreationType) {
            EventCreationType.ADDNEW.name -> {
                programUid?.let { program ->
                    displayOrgUnitSelectorForNewEvent(
                        programUid = program,
                        programStageUid = programStageUid,
                        enrollmentUid = enrollmentUid,
                    )
                }
            }

            else -> {
                goToEventInitial(
                    programStageUid = programStageUid,
                    repeatable = repeatable,
                    periodType = periodType,
                    eventCreationType = eventCreationType,
                    programUid = programUid,
                    enrollmentUid = enrollmentUid,
                )
            }
        }
    }

    private fun displayOrgUnitSelectorForNewEvent(
        programUid: String,
        programStageUid: String,
        enrollmentUid: String?,
    ) {
        OUTreeFragment.Builder()
            .showAsDialog()
            .singleSelection()
            .orgUnitScope(
                OrgUnitSelectorScope.ProgramCaptureScope(programUid),
            )
            .onSelection { selectedOrgUnits ->
                if (selectedOrgUnits.isNotEmpty()) {
                    presenter.onOrgUnitForNewEventSelected(
                        programStageUid = programStageUid,
                        programUid = programUid,
                        orgUnitUid = selectedOrgUnits.first().uid(),
                        enrollmentUid = enrollmentUid,
                    )
                }
            }
            .build()
            .show(supportFragmentManager, "ORG_UNIT_DIALOG")
    }

    override fun goToEventDetails(
        eventUid: String,
        eventMode: EventMode,
        programUid: String,
    ) {
        val intent = EventCaptureActivity.intent(
            context = this,
            eventUid = eventUid,
            programUid = programUid,
            eventMode = eventMode,
        )
        startActivity(intent)
        finish()
    }

    private fun goToEventInitial(
        programStageUid: String,
        repeatable: Boolean,
        periodType: PeriodType?,
        eventCreationType: String?,
        programUid: String?,
        enrollmentUid: String?,
    ) {
        val intent = Intent(this, EventInitialActivity::class.java)
        val bundle = Bundle().apply {
            putString(
                Constants.PROGRAM_UID,
                programUid,
            )
            putString(
                Constants.TRACKED_ENTITY_INSTANCE,
                getIntent().getStringExtra(Constants.TRACKED_ENTITY_INSTANCE),
            )
            putString(
                Constants.ORG_UNIT,
                getIntent().getStringExtra(
                    Constants.ORG_UNIT,
                ),
            )
            putString(
                Constants.ENROLLMENT_UID,
                enrollmentUid,
            )
            putString(
                Constants.EVENT_CREATION_TYPE,
                eventCreationType,
            )
            putBoolean(Constants.EVENT_REPEATABLE, repeatable)
            putSerializable(Constants.EVENT_PERIOD_TYPE, periodType)
            putString(Constants.PROGRAM_STAGE_UID, programStageUid)
            putInt(
                Constants.EVENT_SCHEDULE_INTERVAL,
                presenter.getStandardInterval(programStageUid),
            )
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }

    private fun initInjector() {
        val programId = intent.getStringExtra("PROGRAM_UID")
        val enrollmentId = intent.getStringExtra("ENROLLMENT_UID")
        val eventCreationType = intent.getStringExtra(Constants.EVENT_CREATION_TYPE)
        if (programId == null || enrollmentId == null || eventCreationType == null) {
            finish()
        }
        (applicationContext as App).userComponent()!!
            .plus(
                ProgramStageSelectionModule(
                    this,
                    programId!!,
                    enrollmentId!!,
                    eventCreationType!!,
                ),
            )
            .inject(this)
    }
}

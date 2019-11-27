package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.content.Context
import org.dhis2.App
import org.dhis2.data.tuples.Trio
import org.dhis2.databinding.FragmentIndicatorsBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.hisp.dhis.android.core.program.ProgramIndicator
import javax.inject.Inject

class IndicatorsFragment: FragmentGlobalAbstract(), IndicatorsView {

    @Inject
    private lateinit var presenter: IndicatorsPresenterImpl

    private lateinit var binding: FragmentIndicatorsBinding
    private lateinit var adapter: IndicatorsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = context as TeiDashboardMobileActivity
        if(((context.applicationContext) as App).dashboardComponent() != null){
            ((context.applicationContext) as App).dashboardComponent()!!
                .plus(IndicatorsModule(activity.programUid,
                    activity.teiUid, this))
                .inject(this)
        }
    }

    override fun swapIndicators(indicators: List<Trio<ProgramIndicator, String, String>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
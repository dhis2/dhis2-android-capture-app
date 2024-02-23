package org.dhis2.usescases.enrollment

import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.commons.rules.RuleEngineContextData
import org.hisp.dhis.rules.models.RuleEffect

interface EnrollmentFormRepository {

    fun ruleEngine(): Flowable<RuleEngineContextData>
    fun calculate(): Flowable<Result<List<RuleEffect>>>
    fun generateEvents(): Single<Pair<String, String?>>
    fun getProfilePicture(): String
    fun getProgramStageUidFromEvent(eventUi: String): String?
}

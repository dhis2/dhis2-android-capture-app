package org.dhis2.usescases.enrollment

import io.reactivex.Flowable
import io.reactivex.Single
import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.models.RuleEffect

interface EnrollmentFormRepository {

    fun ruleEngine(): Flowable<RuleEngine>
    fun calculate(): Flowable<Result<List<RuleEffect>>>
    fun generateEvents(): Single<Pair<String, String?>>
    fun getProfilePicture(): String
    fun getProgramStageUidFromEvent(eventUi: String): String?
}

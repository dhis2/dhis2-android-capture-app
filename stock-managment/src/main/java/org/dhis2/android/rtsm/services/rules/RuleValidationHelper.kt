package org.dhis2.android.rtsm.services.rules

import io.reactivex.Flowable
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.android.rtsm.data.models.Transaction
import org.hisp.dhis.rules.models.RuleEffect
import java.util.Date

interface RuleValidationHelper {
    fun evaluate(
        entry: StockEntry, eventDate: Date, program: String,
        transaction: Transaction, eventUid: String? = null,
        appConfig: AppConfig
    ): Flowable<List<RuleEffect>>
}
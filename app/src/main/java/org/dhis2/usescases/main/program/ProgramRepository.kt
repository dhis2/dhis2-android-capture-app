package org.dhis2.usescases.main.program

import io.reactivex.Flowable

internal interface ProgramRepository {

    fun programModels(): Flowable<List<ProgramViewModel>>

    fun aggregatesModels(): Flowable<List<ProgramViewModel>>
}

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

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.dhis2.R
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.tuples.Trio
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DELETE_RELATIONSHIP
import org.dhis2.utils.analytics.NEW_RELATIONSHIP
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipHelper
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance
import org.hisp.dhis.android.core.relationship.RelationshipType
import timber.log.Timber

class RelationshipPresenter(
    val d2: D2,
    val programUid: String?,
    val teiUid: String,
    val dashboardRepository: DashboardRepository,
    private val schedulersProvider: SchedulerProvider,
    var view: RelationshipView,
    val analyticsHelper: AnalyticsHelper
) {

    private val teiType: String? = d2.trackedEntityModule()
        .trackedEntityInstances().byUid().eq(teiUid)
        .one().blockingGet().trackedEntityType()
    private val compositeDisposable = CompositeDisposable()
    val updateRelationships = PublishProcessor.create<Boolean>()

    fun init() {
        updateRelationships()

        relationshipForTeiType()
    }

    fun updateRelationships() {
        compositeDisposable.add(
            updateRelationships.startWith(true)
                .flatMap {
                    Flowable.fromIterable(
                        d2.relationshipModule().relationships().getByItem(
                            RelationshipItem.builder().trackedEntityInstance(
                                RelationshipItemTrackedEntityInstance
                                    .builder()
                                    .trackedEntityInstance(teiUid)
                                    .build()
                            ).build()
                        )
                    ).map { relationship: Relationship ->
                        var relationshipType: RelationshipType? = null
                        d2.relationshipModule().relationshipTypes().blockingGet()
                            .forEach {
                                if (it.uid().equals(relationship.relationshipType()!!)) {
                                    relationshipType = it
                                }
                            }

                        val relationshipTEIUid: String?
                        val direction: RelationshipViewModel.RelationshipDirection
                        if (!teiUid.equals(
                            relationship.from()!!
                                .trackedEntityInstance()!!.trackedEntityInstance()
                        )
                        ) {
                            relationshipTEIUid = relationship.from()!!
                                .trackedEntityInstance()!!.trackedEntityInstance()
                            direction = RelationshipViewModel.RelationshipDirection.FROM
                        } else {
                            relationshipTEIUid = relationship.to()!!
                                .trackedEntityInstance()!!
                                .trackedEntityInstance()
                            direction = RelationshipViewModel
                                .RelationshipDirection.TO
                        }

                        val tei = d2.trackedEntityModule()
                            .trackedEntityInstances()
                            .uid(relationshipTEIUid).blockingGet()

                        val typeAttributes =
                            d2.trackedEntityModule()
                                .trackedEntityTypeAttributes()
                                .byTrackedEntityTypeUid()
                                .eq(tei.trackedEntityType())
                                .byDisplayInList().isTrue
                                .blockingGet()

                        val attributeUids = mutableListOf<String>()

                        for (typeAttribute in typeAttributes) {
                            attributeUids.add(typeAttribute.trackedEntityAttribute()!!.uid())
                        }

                        val attributesValues =
                            d2.trackedEntityModule()
                                .trackedEntityAttributeValues()
                                .byTrackedEntityInstance()
                                .eq(tei.uid())
                                .byTrackedEntityAttribute()
                                .`in`(attributeUids)
                                .blockingGet()

                        return@map RelationshipViewModel.create(
                            relationship,
                            relationshipType!!,
                            direction,
                            relationshipTEIUid,
                            attributesValues
                        )
                    }.toList().toFlowable()
                }
                .subscribeOn(schedulersProvider.io())
                .observeOn(schedulersProvider.ui())
                .subscribe(
                    {
                        view.setRelationships(it)
                    },
                    Timber::d
                )
        )
    }

    fun relationshipForTeiType() {
        compositeDisposable.add(
            dashboardRepository.relationshipsForTeiType(teiType)
                .map {
                    val finalList =
                        mutableListOf<Trio<RelationshipType, String, Int>>()
                    for (rType in it) {
                        val iconResId = dashboardRepository
                            .getObjectStyle(view.abstracContext, rType.val1())
                        finalList.add(Trio.create(rType.val0(), rType.val1(), iconResId))
                    }

                    return@map finalList
                }.subscribeOn(schedulersProvider.io())
                .observeOn(schedulersProvider.ui())
                .subscribe(
                    {
                        view.setRelationshipTypes(it)
                    },
                    Timber::e
                )
        )
    }

    fun goToAddRelationship(teiTypeToAdd: String) {
        if (d2.programModule().programs().uid(programUid).blockingGet().access().data().write()) {
            analyticsHelper.setEvent(NEW_RELATIONSHIP, CLICK, NEW_RELATIONSHIP)
            view.goToAddRelationship(teiUid!!, teiTypeToAdd)
        } else {
            view.displayMessage(view.context.getString(R.string.search_access_error))
        }
    }

    fun deleteRelationship(relationship: Relationship) {
        try {
            d2.relationshipModule().relationships().withItems().uid(relationship.uid())
                .blockingDelete()
        } catch (e: D2Error) {
            Timber.d(e)
        } finally {
            analyticsHelper.setEvent(DELETE_RELATIONSHIP, CLICK, DELETE_RELATIONSHIP)
            updateRelationships.onNext(true)
        }
    }

    fun addRelationship(trackEntityInstance_A: String, relationshipType: String) {
        try {
            val relationship =
                RelationshipHelper.teiToTeiRelationship(
                    teiUid, trackEntityInstance_A, relationshipType
                )
            d2.relationshipModule().relationships().blockingAdd(relationship)
        } catch (e: D2Error) {
            view.displayMessage(e.errorDescription())
        } finally {
            updateRelationships.onNext(true)
        }
    }

    fun openDashboard(tei: String) {
        if (d2.trackedEntityModule().trackedEntityInstances()
            .byUid().eq(tei).one().blockingGet().state() != State.RELATIONSHIP
        ) {
            if (d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(tei).blockingGet().isNotEmpty()
            ) {
                view.goToTeiDashboard(tei)
            } else {
                view.showDialogRelationshipWithoutEnrollment(
                    d2.trackedEntityModule()
                        .trackedEntityTypes().uid(teiType).blockingGet().displayName()!!
                )
            }
        } else {
            view.showDialogRelationshipNotFoundMessage(
                d2.trackedEntityModule()
                    .trackedEntityTypes().uid(teiType).blockingGet().displayName()!!
            )
        }
    }

    fun onDettach() {
        compositeDisposable.clear()
    }

    fun displayMessage(message: String) {
        view.displayMessage(message)
    }
}

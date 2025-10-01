package org.dhis2.usescases.qrReader

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.tracker.exporter.TrackerD2Progress
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

internal class QrReaderPresenterImpl(
    private val d2: D2,
    private val schedulerProvider: SchedulerProvider,
) : QrReaderContracts.Presenter {
    private var view: QrReaderContracts.View? = null
    private val compositeDisposable = CompositeDisposable()

    private var eventWORegistrationJson: JSONObject? = null
    private var eventUid: String? = null
    private val dataJson = ArrayList<JSONObject>()
    private val teiDataJson = ArrayList<JSONObject>()

    private var teiJson: JSONObject? = null
    private val attrJson: MutableList<JSONArray> = ArrayList()
    private val enrollmentJson: MutableList<JSONArray> = ArrayList()
    private val relationshipsJson: MutableList<JSONArray> = ArrayList()
    private val eventsJson = ArrayList<JSONObject>()
    private var teiUid: String? = null

    override fun handleEventWORegistrationInfo(jsonObject: JSONObject) {
        this.eventWORegistrationJson = jsonObject
        eventUid = null
        try {
            eventUid = jsonObject.getString("uid")
        } catch (e: JSONException) {
            Timber.e(e)
        }

        view!!.renderEventWORegistrationInfo(eventUid)
    }

    override fun handleDataWORegistrationInfo(jsonArray: JSONArray) {
        var attributes = ArrayList<Triple<TrackedEntityDataValue, String?, Boolean>>()
        if (eventUid != null) {
            try {
                // LOOK FOR TRACKED ENTITY ATTRIBUTES ON LOCAL DATABASE
                for (i in 0..<jsonArray.length()) {
                    val attrValue = jsonArray.getJSONObject(i)
                    val trackedEntityDataValueModelBuilder =
                        getTrackedEntityDataValueModelBuilderForWORegistration(attrValue)

                    if (attrValue.has("dataElement") && attrValue.getString("dataElement") != null) {
                        // LOOK FOR dataElement ON LOCAL DATABASE.
                        // IF FOUND, OPEN DASHBOARD
                        attributes =
                            lookForDataElementInLocalDatabase(
                                attrValue,
                                trackedEntityDataValueModelBuilder,
                            )
                    } else {
                        attributes.add(
                            Triple<TrackedEntityDataValue, String?, Boolean>(
                                trackedEntityDataValueModelBuilder.build(),
                                null,
                                false,
                            ),
                        )
                    }
                }
            } catch (e: JSONException) {
                Timber.e(e)
            } catch (e: ParseException) {
                Timber.e(e)
            }
        }

        view!!.renderEventDataInfo(attributes)
    }

    private fun lookForDataElementInLocalDatabase(
        attrValue: JSONObject,
        trackedEntityDataValueModelBuilder: TrackedEntityDataValue.Builder,
    ): ArrayList<Triple<TrackedEntityDataValue, String?, Boolean>> {
        val attributes = ArrayList<Triple<TrackedEntityDataValue, String?, Boolean>>()

        if (d2
                .dataElementModule()
                .dataElements()
                .uid(attrValue.getString("dataElement"))
                .blockingExists()
        ) {
            dataJson.add(attrValue)
            val de =
                d2
                    .dataElementModule()
                    .dataElements()
                    .uid(attrValue.getString("dataElement"))
                    .blockingGet()
            attributes.add(
                Triple(
                    trackedEntityDataValueModelBuilder.build(),
                    de!!.displayFormName(),
                    true,
                ),
            )
        } else {
            attributes.add(
                Triple<TrackedEntityDataValue, String?, Boolean>(
                    trackedEntityDataValueModelBuilder.build(),
                    null,
                    false,
                ),
            )
        }
        return attributes
    }

    private fun getTrackedEntityDataValueModelBuilderForWORegistration(attrValue: JSONObject): TrackedEntityDataValue.Builder {
        val trackedEntityDataValueModelBuilder = TrackedEntityDataValue.builder()

        val simpleDateFormat =
            SimpleDateFormat(DateUtils.DATABASE_FORMAT_EXPRESSION, Locale.getDefault())
        trackedEntityDataValueModelBuilder.event(eventUid)
        if (attrValue.has("dataElement")) {
            trackedEntityDataValueModelBuilder.dataElement(attrValue.getString("dataElement"))
        }
        if (attrValue.has("storedBy")) {
            trackedEntityDataValueModelBuilder.storedBy(attrValue.getString("storedBy"))
        }
        if (attrValue.has("value")) {
            trackedEntityDataValueModelBuilder.value(attrValue.getString("value"))
        }
        if (attrValue.has("providedElsewhere")) {
            trackedEntityDataValueModelBuilder.providedElsewhere(
                attrValue.getString("providedElsewhere").toBoolean(),
            )
        }
        if (attrValue.has("created")) {
            trackedEntityDataValueModelBuilder.created(
                simpleDateFormat.parse(
                    attrValue.getString(
                        "created",
                    ),
                ),
            )
        }
        if (attrValue.has("lastUpdated")) {
            trackedEntityDataValueModelBuilder.lastUpdated(
                simpleDateFormat.parse(
                    attrValue.getString("lastUpdated"),
                ),
            )
        }

        return trackedEntityDataValueModelBuilder
    }

    override fun handleDataInfo(jsonArray: JSONArray) {
        val attributes = ArrayList<Triple<TrackedEntityDataValue, String?, Boolean>>()
        try {
            // LOOK FOR TRACKED ENTITY ATTRIBUTES ON LOCAL DATABASE
            for (i in 0..<jsonArray.length()) {
                val attrValue = jsonArray.getJSONObject(i)
                val trackedEntityDataValueModelBuilder =
                    getTrackedEntityDataValueModelBuilder(attrValue)

                if (attrValue.has("dataElement") && attrValue.getString("dataElement") != null) {
                    // LOOK FOR dataElement ON LOCAL DATABASE.
                    // IF FOUND, OPEN DASHBOARD
                    if (d2
                            .dataElementModule()
                            .dataElements()
                            .uid(attrValue.getString("dataElement"))
                            .blockingExists()
                    ) {
                        teiDataJson.add(attrValue)
                        val de =
                            d2
                                .dataElementModule()
                                .dataElements()
                                .uid(attrValue.getString("dataElement"))
                                .blockingGet()
                        attributes.add(
                            Triple(
                                trackedEntityDataValueModelBuilder.build(),
                                de!!.displayFormName(),
                                true,
                            ),
                        )
                    } else {
                        attributes.add(
                            Triple<TrackedEntityDataValue, String?, Boolean>(
                                trackedEntityDataValueModelBuilder.build(),
                                null,
                                false,
                            ),
                        )
                    }
                } else {
                    attributes.add(
                        Triple<TrackedEntityDataValue, String?, Boolean>(
                            trackedEntityDataValueModelBuilder.build(),
                            null,
                            false,
                        ),
                    )
                }
            }
        } catch (e: JSONException) {
            Timber.e(e)
        } catch (e: ParseException) {
            Timber.e(e)
        }

        view!!.renderTeiEventDataInfo(attributes)
    }

    private fun getTrackedEntityDataValueModelBuilder(attrValue: JSONObject): TrackedEntityDataValue.Builder {
        val trackedEntityDataValueModelBuilder =
            TrackedEntityDataValue.builder()
        val simpleDateFormat =
            SimpleDateFormat(DateUtils.DATABASE_FORMAT_EXPRESSION, Locale.getDefault())

        if (attrValue.has("event")) {
            trackedEntityDataValueModelBuilder.event(attrValue.getString("event"))
        }
        if (attrValue.has("dataElement")) {
            trackedEntityDataValueModelBuilder.dataElement(attrValue.getString("dataElement"))
        }
        if (attrValue.has("storedBy")) {
            trackedEntityDataValueModelBuilder.storedBy(attrValue.getString("storedBy"))
        }
        if (attrValue.has("value")) {
            trackedEntityDataValueModelBuilder.value(attrValue.getString("value"))
        }
        if (attrValue.has("providedElsewhere")) {
            trackedEntityDataValueModelBuilder.providedElsewhere(
                attrValue.getString("providedElsewhere").toBoolean(),
            )
        }
        if (attrValue.has("created")) {
            trackedEntityDataValueModelBuilder.created(
                simpleDateFormat.parse(
                    attrValue.getString(
                        "created",
                    ),
                ),
            )
        }
        if (attrValue.has("lastUpdated")) {
            trackedEntityDataValueModelBuilder.lastUpdated(
                simpleDateFormat.parse(
                    attrValue.getString(
                        "lastUpdated",
                    ),
                ),
            )
        }
        return trackedEntityDataValueModelBuilder
    }

    override fun handleTeiInfo(jsonObject: JSONObject) {
        this.teiJson = jsonObject
        teiUid = null
        try {
            teiUid = jsonObject.getString("uid")
        } catch (e: JSONException) {
            Timber.e(e)
        }

        // IF TEI READ
        teiUid?.let { currentTeiUid ->
            // 'it' will be the non-null teiUid
            // IF FOUND, OPEN DASHBOARD
            if (d2
                    .trackedEntityModule()
                    .trackedEntityInstances()
                    .uid(currentTeiUid)
                    .blockingExists()
            ) {
                view!!.goToDashBoard(currentTeiUid)
            } else {
                view!!.downloadTei(currentTeiUid)
            }
        } ?: view!!.renderTeiInfo(null)
    }

    override fun handleAttrInfo(jsonArray: JSONArray) {
        attrJson.add(jsonArray)
        val attributes = ArrayList<Triple<String, String, Boolean>>()
        try {
            // LOOK FOR TRACKED ENTITY ATTRIBUTES ON LOCAL DATABASE
            for (i in 0..<jsonArray.length()) {
                val attrValue = jsonArray.getJSONObject(i)
                if (attrValue.has("trackedEntityAttribute") && attrValue.getString("trackedEntityAttribute") != null) {
                    // TRACKED ENTITY ATTRIBUTE FOUND, TRACKED ENTITY ATTRIBUTE VALUE CAN BE SAVED.
                    if (d2
                            .trackedEntityModule()
                            .trackedEntityAttributes()
                            .uid(attrValue.getString("trackedEntityAttribute"))
                            .blockingExists()
                    ) {
                        val attribute =
                            d2
                                .trackedEntityModule()
                                .trackedEntityAttributes()
                                .uid(attrValue.getString("trackedEntityAttribute"))
                                .blockingGet()
                        attributes.add(
                            Triple<String, String, Boolean>(
                                attribute!!.displayName()!!,
                                attrValue.getString("value"),
                                true,
                            ),
                        )
                    } else {
                        attributes.add(
                            Triple(
                                attrValue.getString("trackedEntityAttribute"),
                                "",
                                false,
                            ),
                        )
                    }
                }
            }
        } catch (e: JSONException) {
            Timber.e(e)
        }

        view!!.renderAttrInfo(attributes)
    }

    override fun handleEnrollmentInfo(jsonArray: JSONArray) {
        enrollmentJson.add(jsonArray)
        val enrollments = ArrayList<Pair<String, Boolean>>()
        try {
            // LOOK FOR PROGRAM ON LOCAL DATABASE
            for (i in 0..<jsonArray.length()) {
                val attrValue = jsonArray.getJSONObject(i)
                if (attrValue.has("program") && attrValue.getString("program") != null) {
                    // PROGRAM FOUND, ENROLLMENT CAN BE SAVED
                    if (d2
                            .programModule()
                            .programs()
                            .uid(attrValue.getString("program"))
                            .blockingExists()
                    ) {
                        val program =
                            d2
                                .programModule()
                                .programs()
                                .uid(attrValue.getString("program"))
                                .blockingGet()
                        enrollments.add(Pair(program!!.displayName()!!, true))
                    } else {
                        enrollments.add(Pair(attrValue.getString("uid"), false))
                    }
                }
            }
        } catch (e: JSONException) {
            Timber.e(e)
        }

        view!!.renderEnrollmentInfo(enrollments)
    }

    override fun handleEventInfo(jsonObject: JSONObject) {
        eventsJson.add(jsonObject)
        val events = ArrayList<Pair<String, Boolean>>()
        try {
            // LOOK FOR ENROLLMENT ON LOCAL DATABASE
            if (jsonObject.has("enrollment") && jsonObject.getString("enrollment") != null) {
                // ENROLLMENT FOUND, EVENT CAN BE SAVED
                if (d2
                        .enrollmentModule()
                        .enrollments()
                        .uid(jsonObject.getString("enrollment"))
                        .blockingExists()
                ) {
                    events.add(Pair(jsonObject.getString("enrollment"), true))
                } else {
                    val isEnrollmentReadFromQr = checkIfEnrollmentReadFromQr(jsonObject)

                    if (isEnrollmentReadFromQr) {
                        events.add(Pair(jsonObject.getString("uid"), true))
                    } else {
                        events.add(Pair(jsonObject.getString("uid"), false))
                    }
                }
            }
        } catch (e: JSONException) {
            Timber.e(e)
        }

        view!!.renderEventInfo(events)
    }

    private fun checkIfEnrollmentReadFromQr(jsonObject: JSONObject): Boolean {
        var isEnrollmentReadFromQr = false
        for (i in enrollmentJson.indices) {
            val enrollmentArray = enrollmentJson[i]
            for (j in 0..<enrollmentArray.length()) {
                val enrollment = enrollmentArray.getJSONObject(j)
                if (jsonObject.getString("enrollment") == enrollment.getString("uid")) {
                    isEnrollmentReadFromQr = true
                    break
                }
            }
        }
        return isEnrollmentReadFromQr
    }

    override fun handleRelationship(jsonArray: JSONArray) {
        relationshipsJson.add(jsonArray)
        val relationships = ArrayList<Pair<String, Boolean>>()

        for (i in 0..<jsonArray.length()) {
            try {
                val relationship = jsonArray.getJSONObject(i)
                relationships.add(Pair(relationship.getString("trackedEntityInstanceA"), true))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        view!!.renderRelationship(relationships)
    }

    override fun init(view: QrReaderContracts.View) {
        this.view = view
    }

    // SAVES READ TRACKED ENTITY INSTANCE, TRACKED ENTITY ATTRIBUTE VALUES, ENROLLMENTS, EVENTS AND RELATIONSHIPS INTO LOCAL DATABASE
    override fun download() {
        saveTEI()
        for (i in attrJson.indices) {
            val attrArray = attrJson[i]
            for (j in 0..<attrArray.length()) {
                saveTrackedEntityAttributeValue(attrArray.getJSONObject(j))
            }
        }

        /* TODO: CHANGE RELATIONSHIPS
         for (i in relationshipsJson.indices) {

           try {
            JSONObject relationship = relationshipsJson.getJSONObject(i);


            RelationshipModel.Builder relationshipModelBuilder;
            relationshipModelBuilder = RelationshipModel.builder();

            if (relationship.has("trackedEntityInstanceA"))
                relationshipModelBuilder.trackedEntityInstanceA(relationship.getString("trackedEntityInstanceA"));
            if (relationship.has("trackedEntityInstanceB"))
                relationshipModelBuilder.trackedEntityInstanceB(relationship.getString("trackedEntityInstanceB"));
            if (relationship.has("relationshipType"))
                relationshipModelBuilder.relationshipType(relationship.getString("relationshipType"));

            RelationshipModel relationshipModel = relationshipModelBuilder.build();

            if (relationshipModel != null)
                writeDatabase.insert(RelationshipModel.TABLE, relationshipModel.toContentValues());

        } catch (Exception e) {
            Timber.e(e);
        }
            }
*/

        for (i in enrollmentJson.indices) {
            val enrollmentArray = enrollmentJson[i]
            for (j in 0..<enrollmentArray.length()) {
                saveEnrollment(enrollmentArray.getJSONObject(j))
            }
        }

        for (i in eventsJson.indices) {
            saveEvent(eventsJson[i])
        }

        for (i in teiDataJson.indices) {
            saveTEIAttributes(teiDataJson[i])
        }
        view!!.goToDashBoard(teiUid)
    }

    private fun saveTEIAttributes(attrV: JSONObject) {
        try {
            val attrValueModelBuilder =
                TrackedEntityDataValue.builder()

            if (attrV.has("event")) attrValueModelBuilder.event(attrV.getString("event"))
            if (attrV.has("lastUpdated")) {
                attrValueModelBuilder.lastUpdated(
                    DateUtils.databaseDateFormat().parse(attrV.getString("lastUpdated")),
                )
            }
            if (attrV.has("dataElement")) attrValueModelBuilder.dataElement(attrV.getString("dataElement"))
            if (attrV.has("storedBy")) attrValueModelBuilder.storedBy(attrV.getString("storedBy"))
            if (attrV.has("value")) attrValueModelBuilder.value(attrV.getString("value"))
            if (attrV.has("providedElsewhere")) {
                attrValueModelBuilder.providedElsewhere(
                    attrV
                        .getString(
                            "providedElsewhere",
                        ).toBoolean(),
                )
            }

            val attrValueModel = attrValueModelBuilder.build()

            if (attrValueModel != null) {
                val result =
                    runBlocking {
                        d2
                            .databaseAdapter()
                            .upsertObject(attrValueModel, TrackedEntityDataValue::class)
                    }
                Timber.d("upsert event ${result?.name}")
            }
        } catch (e: JSONException) {
            Timber.e(e)
        } catch (e: ParseException) {
            Timber.e(e)
        }
    }

    private fun saveTEI() {
        try {
            if (teiJson != null) {
                val teiModelBuilder = getTrackedEntityInstanceBuilder(teiJson!!)
                val teiModel =
                    teiModelBuilder
                        .deleted(false)
                        .build()

                if (teiModel != null) {
                    runBlocking {
                        d2.databaseAdapter().upsertObject(teiModel, TrackedEntityInstance::class)
                    }
                }
            } else {
                view!!.showIdError()
                return
            }
        } catch (e: JSONException) {
            Timber.e(e)
        } catch (e: ParseException) {
            Timber.e(e)
        }
    }

    private fun saveEvent(eventJson: JSONObject) {
        try {
            var eventBuilder =
                Event.builder()
            if (eventJson.has("uid")) eventBuilder.uid(eventJson.getString("uid"))
            if (eventJson.has("created")) {
                eventBuilder.created(
                    DateUtils.databaseDateFormat().parse(eventJson.getString("created")),
                )
            }
            if (eventJson.has("lastUpdated")) {
                eventBuilder.lastUpdated(
                    DateUtils.databaseDateFormat().parse(eventJson.getString("lastUpdated")),
                )
            }
            if (eventJson.has("aggregatedSyncState")) {
                eventBuilder.aggregatedSyncState(
                    State.valueOf(eventJson.getString("aggregatedSyncState")),
                )
            }
            if (eventJson.has("enrollment")) eventBuilder.enrollment(eventJson.getString("enrollment"))
            if (eventJson.has("program")) eventBuilder.program(eventJson.getString("program"))
            if (eventJson.has("programStage")) {
                eventBuilder.programStage(
                    eventJson.getString(
                        "programStage",
                    ),
                )
            }
            if (eventJson.has("organisationUnit")) {
                eventBuilder.organisationUnit(
                    eventJson.getString(
                        "organisationUnit",
                    ),
                )
            }
            if (eventJson.has("eventDate")) {
                eventBuilder.eventDate(
                    DateUtils.databaseDateFormat().parse(eventJson.getString("eventDate")),
                )
            }
            if (eventJson.has("status")) {
                eventBuilder.status(
                    EventStatus.valueOf(
                        eventJson.getString(
                            "status",
                        ),
                    ),
                )
            }
            if (eventJson.has("attributeOptionCombo")) {
                eventBuilder.attributeOptionCombo(
                    eventJson.getString("attributeOptionCombo"),
                )
            }
            eventBuilder = checkEventGeometry(eventJson, eventBuilder)
            eventBuilder = checkEventCompletedDate(eventJson, eventBuilder)

            if (eventJson.has("dueDate")) {
                eventBuilder.dueDate(
                    DateUtils.databaseDateFormat().parse(eventJson.getString("dueDate")),
                )
            }

            val eventModel =
                eventBuilder
                    .deleted(false)
                    .build()

            if (eventModel != null) {
                runBlocking { d2.databaseAdapter().upsertObject(eventModel, Event::class) }
            }
        } catch (e: JSONException) {
            Timber.e(e)
        } catch (e: ParseException) {
            Timber.e(e)
        }
    }

    private fun checkEventCompletedDate(
        eventJson: JSONObject,
        eventBuilder: Event.Builder,
    ): Event.Builder =
        if (eventJson.has("completedDate")) {
            eventBuilder.completedDate(
                DateUtils.databaseDateFormat().parse(eventJson.getString("completedDate")),
            )
        } else {
            eventBuilder
        }

    private fun checkEventGeometry(
        eventJson: JSONObject,
        eventBuilder: Event.Builder,
    ): Event.Builder =
        if (eventJson.has("geometry")) {
            eventBuilder.geometry(
                Geometry
                    .builder()
                    .type(
                        FeatureType.valueOf(
                            eventJson.getJSONObject("geometry").getString("type"),
                        ),
                    ).coordinates(
                        eventJson.getJSONObject("geometry").getString("coordinates"),
                    ).build(),
            )
        } else {
            eventBuilder
        }

    private fun saveEnrollment(enrollmentJson: JSONObject) {
        try {
            val enrollmentBuilder =
                Enrollment.builder()
            if (enrollmentJson.has("uid")) {
                enrollmentBuilder.uid(
                    enrollmentJson.getString(
                        "uid",
                    ),
                )
            }
            if (enrollmentJson.has("created")) {
                enrollmentBuilder.created(
                    DateUtils
                        .databaseDateFormat()
                        .parse(enrollmentJson.getString("created")),
                )
            }
            if (enrollmentJson.has("lastUpdated")) {
                enrollmentBuilder.lastUpdated(
                    DateUtils
                        .databaseDateFormat()
                        .parse(enrollmentJson.getString("lastUpdated")),
                )
            }
            if (enrollmentJson.has("aggregatedSyncState")) {
                enrollmentBuilder.aggregatedSyncState(
                    State.valueOf(enrollmentJson.getString("aggregatedSyncState")),
                )
            }
            if (enrollmentJson.has("program")) {
                enrollmentBuilder.program(
                    enrollmentJson.getString(
                        "program",
                    ),
                )
            }
            if (enrollmentJson.has("followUp")) {
                enrollmentBuilder.followUp(
                    enrollmentJson.getBoolean("followUp"),
                )
            }
            if (enrollmentJson.has("status")) {
                enrollmentBuilder.status(
                    EnrollmentStatus.valueOf(
                        enrollmentJson.getString("status"),
                    ),
                )
            }
            if (enrollmentJson.has("enrollmentDate")) {
                enrollmentBuilder.enrollmentDate(
                    DateUtils
                        .databaseDateFormat()
                        .parse(enrollmentJson.getString("enrollmentDate")),
                )
            }
            if (enrollmentJson.has("incidentDate")) {
                enrollmentBuilder.incidentDate(
                    DateUtils
                        .databaseDateFormat()
                        .parse(enrollmentJson.getString("incidentDate")),
                )
            }
            if (enrollmentJson.has("organisationUnit")) {
                enrollmentBuilder.organisationUnit(
                    enrollmentJson.getString("organisationUnit"),
                )
            }
            if (enrollmentJson.has("trackedEntityInstance")) {
                enrollmentBuilder.trackedEntityInstance(
                    enrollmentJson.getString("trackedEntityInstance"),
                )
            }
            if (enrollmentJson.has("geometry")) {
                enrollmentBuilder.geometry(
                    Geometry
                        .builder()
                        .type(
                            FeatureType.valueOf(
                                enrollmentJson.getJSONObject("geometry").getString("type"),
                            ),
                        ).coordinates(
                            enrollmentJson
                                .getJSONObject("geometry")
                                .getString("coordinates"),
                        ).build(),
                )
            }

            val enrollment =
                enrollmentBuilder
                    .deleted(false)
                    .build()

            if (enrollment != null) {
                runBlocking {
                    d2.databaseAdapter().upsertObject(enrollment, Enrollment::class)
                }
            }
        } catch (e: JSONException) {
            Timber.e(e)
        } catch (e: ParseException) {
            Timber.e(e)
        }
    }

    private fun saveTrackedEntityAttributeValue(attrV: JSONObject) {
        try {
            val attrValueModelBuilder =
                TrackedEntityAttributeValue.builder()
            if (attrV.has("created")) {
                attrValueModelBuilder.created(
                    DateUtils.databaseDateFormat().parse(attrV.getString("created")),
                )
            }
            if (attrV.has("lastUpdated")) {
                attrValueModelBuilder.lastUpdated(
                    DateUtils.databaseDateFormat().parse(attrV.getString("lastUpdated")),
                )
            }
            if (attrV.has("value")) attrValueModelBuilder.value(attrV.getString("value"))
            if (attrV.has("trackedEntityInstance")) {
                attrValueModelBuilder.trackedEntityInstance(
                    attrV.getString("trackedEntityInstance"),
                )
            }
            if (attrV.has("trackedEntityAttribute")) {
                attrValueModelBuilder.trackedEntityAttribute(
                    attrV.getString("trackedEntityAttribute"),
                )
            }

            val attrValueModel = attrValueModelBuilder.build()

            if (attrValueModel != null) {
                runBlocking {
                    d2
                        .databaseAdapter()
                        .upsertObject(attrValueModel, TrackedEntityAttributeValue::class)
                }
            }
        } catch (e: JSONException) {
            Timber.e(e)
        } catch (e: ParseException) {
            Timber.e(e)
        }
    }

    private fun getTrackedEntityInstanceBuilder(teiJson: JSONObject): TrackedEntityInstance.Builder {
        val teiModelBuilder = TrackedEntityInstance.builder()
        if (teiJson.has("uid")) teiModelBuilder.uid(teiJson.getString("uid"))
        if (teiJson.has("created")) {
            teiModelBuilder.created(
                DateUtils.databaseDateFormat().parse(
                    teiJson.getString("created"),
                ),
            )
        }
        if (teiJson.has("lastUpdated")) {
            teiModelBuilder.lastUpdated(
                DateUtils.databaseDateFormat().parse(
                    teiJson.getString("lastUpdated"),
                ),
            )
        }
        if (teiJson.has("aggregatedSyncState")) {
            teiModelBuilder.aggregatedSyncState(
                State.valueOf(
                    teiJson.getString("aggregatedSyncState"),
                ),
            )
        }
        if (teiJson.has("organisationUnit")) {
            teiModelBuilder.organisationUnit(
                teiJson.getString(
                    "organisationUnit",
                ),
            )
        }
        if (teiJson.has("trackedEntityType")) {
            teiModelBuilder.trackedEntityType(
                teiJson.getString(
                    "trackedEntityType",
                ),
            )
        }
        if (teiJson.has("geometry")) {
            teiModelBuilder.geometry(
                Geometry
                    .builder()
                    .type(
                        FeatureType.valueOf(
                            teiJson.getJSONObject("geometry").getString("type"),
                        ),
                    ).coordinates(teiJson.getJSONObject("geometry").getString("coordinates"))
                    .build(),
            )
        }
        return teiModelBuilder
    }

    // CALLS THE ENDPOINT TO DOWNLOAD AND SAVE THE TRACKED ENTITY INSTANCE INFO
    override fun onlineDownload() {
        view!!.initDownload()
        val uidToDownload = teiUid?.let { listOf(it) } ?: emptyList()
        compositeDisposable.add(
            d2
                .trackedEntityModule()
                .trackedEntityInstanceDownloader()
                .byUid()
                .`in`(uidToDownload)
                .download()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    Consumer { _: TrackerD2Progress? -> },
                    Consumer { _: Throwable? ->
                        view!!.finishDownload()
                        view!!.renderTeiInfo(teiUid)
                    },
                ) {
                    view!!.finishDownload()
                    if (d2
                            .trackedEntityModule()
                            .trackedEntityInstances()
                            .uid(teiUid)
                            .blockingExists()
                    ) {
                        view!!.goToDashBoard(teiUid)
                    } else {
                        view!!.renderTeiInfo(teiUid)
                    }
                },
        )
    }

    override fun dispose() {
        compositeDisposable.clear()
    }

    override fun downloadEventWORegistration() {
        var programUid: String? = null
        var orgUnit: String? = null
        try {
            if (eventWORegistrationJson != null) {
                programUid = eventWORegistrationJson!!.getString("program")
                orgUnit = eventWORegistrationJson!!.getString("organisationUnit")

                val eventBuilder = mapBuilderFromJson(eventWORegistrationJson!!)

                eventBuilder.aggregatedSyncState(State.TO_UPDATE)

                val event =
                    eventBuilder
                        .deleted(false)
                        .build()

                if (!d2
                        .eventModule()
                        .events()
                        .uid(event.uid())
                        .blockingExists()
                ) {
                    val result =
                        runBlocking { d2.databaseAdapter().upsertObject(event, Event::class) }
                    Timber.d("insert event ${result?.name}")
                }
            } else {
                view!!.showIdError()
                return
            }
        } catch (e: JSONException) {
            Timber.e(e)
        } catch (e: ParseException) {
            Timber.e(e)
        }

        for (i in dataJson.indices) {
            try {
                val attrV = dataJson[i]
                val attrValueModelBuilder = mapAttributeValueBuilderFromJson(attrV)
                val attrValueModel = attrValueModelBuilder.build()

                if (attrValueModel != null) {
                    val result =
                        runBlocking {
                            d2
                                .databaseAdapter()
                                .upsertObject(attrValueModel, TrackedEntityDataValue::class)
                        }
                    Timber.d("insert event ${result?.name}")
                }
            } catch (e: JSONException) {
                Timber.e(e)
            } catch (e: ParseException) {
                Timber.e(e)
            }
        }

        view!!.goToEvent(eventUid, programUid, orgUnit)
    }

    private fun mapAttributeValueBuilderFromJson(attrV: JSONObject): TrackedEntityDataValue.Builder {
        val attrValueModelBuilder = TrackedEntityDataValue.builder()

        if (attrV.has("event")) attrValueModelBuilder.event(attrV.getString("event"))
        if (attrV.has("lastUpdated")) {
            attrValueModelBuilder.lastUpdated(
                DateUtils.databaseDateFormat().parse(attrV.getString("lastUpdated")),
            )
        }
        if (attrV.has("dataElement")) attrValueModelBuilder.dataElement(attrV.getString("dataElement"))
        if (attrV.has("storedBy")) attrValueModelBuilder.storedBy(attrV.getString("storedBy"))
        if (attrV.has("value")) attrValueModelBuilder.value(attrV.getString("value"))
        if (attrV.has("providedElsewhere")) {
            attrValueModelBuilder.providedElsewhere(
                attrV
                    .getString(
                        "providedElsewhere",
                    ).toBoolean(),
            )
        }
        return attrValueModelBuilder
    }

    private fun mapBuilderFromJson(eventWORegistrationJson: JSONObject): Event.Builder {
        var eventBuilder = Event.builder()
        eventBuilder = checkEventWORegistrationUid(eventWORegistrationJson, eventBuilder)
        eventBuilder = checkEventWOEnrollment(eventWORegistrationJson, eventBuilder)

        if (eventWORegistrationJson.has("created")) {
            eventBuilder.created(
                DateUtils.databaseDateFormat().parse(
                    eventWORegistrationJson.getString("created"),
                ),
            )
        }
        if (eventWORegistrationJson.has("lastUpdated")) {
            eventBuilder.lastUpdated(
                DateUtils.databaseDateFormat().parse(
                    eventWORegistrationJson.getString("lastUpdated"),
                ),
            )
        }
        if (eventWORegistrationJson.has("createdAtClient")) {
            eventBuilder.createdAtClient(
                DateUtils.databaseDateFormat().parse(
                    eventWORegistrationJson.getString("createdAtClient"),
                ),
            )
        }
        if (eventWORegistrationJson.has("lastUpdatedAtClient")) {
            eventBuilder.lastUpdatedAtClient(
                DateUtils.databaseDateFormat().parse(
                    eventWORegistrationJson.getString("lastUpdatedAtClient"),
                ),
            )
        }
        if (eventWORegistrationJson.has("status")) {
            eventBuilder.status(EventStatus.valueOf(eventWORegistrationJson.getString("status")))
        }
        if (eventWORegistrationJson.has("geometry")) { // TODO: FIX QRs -> SHOULD USE SMS COMPRESSION LIBRARY
            eventBuilder.geometry(
                Geometry
                    .builder()
                    .type(
                        FeatureType.valueOf(
                            eventWORegistrationJson
                                .getJSONObject("geometry")
                                .getString("type"),
                        ),
                    ).coordinates(
                        eventWORegistrationJson
                            .getJSONObject("geometry")
                            .getString("coordinates"),
                    ).build(),
            )
        }
        if (eventWORegistrationJson.has("program")) {
            eventBuilder.program(eventWORegistrationJson.getString("program"))
        }
        if (eventWORegistrationJson.has("programStage")) {
            eventBuilder.programStage(eventWORegistrationJson.getString("programStage"))
        }
        if (eventWORegistrationJson.has("programStage")) {
            eventBuilder.programStage(eventWORegistrationJson.getString("programStage"))
        }
        if (eventWORegistrationJson.has("organisationUnit")) {
            eventBuilder.organisationUnit(eventWORegistrationJson.getString("organisationUnit"))
        }
        if (eventWORegistrationJson.has("eventDate")) {
            eventBuilder.eventDate(
                DateUtils.databaseDateFormat().parse(
                    eventWORegistrationJson.getString("eventDate"),
                ),
            )
        }
        if (eventWORegistrationJson.has("completedDate")) {
            eventBuilder.completedDate(
                DateUtils.databaseDateFormat().parse(
                    eventWORegistrationJson.getString("completedDate"),
                ),
            )
        }
        if (eventWORegistrationJson.has("dueDate")) {
            eventBuilder.dueDate(
                DateUtils.databaseDateFormat().parse(
                    eventWORegistrationJson.getString("dueDate"),
                ),
            )
        }
        if (eventWORegistrationJson.has("attributeOptionCombo")) {
            eventBuilder.attributeOptionCombo(eventWORegistrationJson.getString("attributeOptionCombo"))
        }
        return eventBuilder
    }

    private fun checkEventWOEnrollment(
        eventWORegistrationJson: JSONObject,
        eventBuilder: Event.Builder,
    ): Event.Builder =
        if (eventWORegistrationJson.has("enrollment")) {
            eventBuilder.enrollment(eventWORegistrationJson.getString("enrollment"))
        } else {
            eventBuilder
        }

    private fun checkEventWORegistrationUid(
        eventWORegistrationJson: JSONObject,
        builder: Event.Builder,
    ): Event.Builder =
        if (eventWORegistrationJson.has("uid")) {
            builder.uid(eventWORegistrationJson.getString("uid"))
        } else {
            builder
        }
}

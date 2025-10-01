package org.dhis2.data.qr

import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.SingleSource
import io.reactivex.functions.Function
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.date.DateUtils
import org.dhis2.usescases.qrCodes.QrViewModel
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Coordinates
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
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.util.Calendar
import java.util.Date
import java.util.concurrent.Callable
import java.util.regex.Pattern
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

// TODO: CHANGE THIS TO GET INFO FROM SMS Library
class QRCodeGenerator(
    private val d2: D2,
) : QRInterface {
    private val gson: Gson =
        GsonBuilder().setDateFormat(DateUtils.DATABASE_FORMAT_EXPRESSION).create()

    override fun teiQRs(teiUid: String): Observable<List<QrViewModel>> {
        var bitmaps: MutableList<QrViewModel> = ArrayList()

        return d2
            .trackedEntityModule()
            .trackedEntityInstances()
            .uid(teiUid)
            .get()
            .map(
                Function { data: TrackedEntityInstance? ->
                    bitmaps.add(
                        QrViewModel(
                            QRjson.TEI_JSON,
                            gson.toJson(data),
                        ),
                    )
                },
            ).flatMap(
                Function<Boolean, SingleSource<out List<TrackedEntityAttributeValue>>> { _: Boolean? ->
                    d2
                        .trackedEntityModule()
                        .trackedEntityAttributeValues()
                        .byTrackedEntityInstance()
                        .eq(teiUid)
                        .get()
                },
            ).map(
                Function { data: List<TrackedEntityAttributeValue> ->
                    // DIVIDE ATTR QR GENERATION -> 1 QR PER 2 ATTR
                    bitmaps = divideAttributesInQrGeneration(bitmaps, data)
                    true
                },
            ).flatMap(
                Function<Boolean, SingleSource<out List<Enrollment>>> { _: Boolean? ->
                    d2
                        .enrollmentModule()
                        .enrollments()
                        .byTrackedEntityInstance()
                        .eq(teiUid)
                        .get()
                },
            ).map(
                Function { data: List<Enrollment> ->
                    // DIVIDE ENROLLMENT QR GENERATION -> 1 QR PER 2 ENROLLMENT
                    bitmaps = divideEnrollmentsInQrGeneration(bitmaps, data)

                    data
                },
            ).toObservable()
            .flatMap(
                Function<List<Enrollment>, ObservableSource<out List<Event>>> { data: List<Enrollment>? ->
                    Observable.fromIterable(data).flatMap(
                        Function<Enrollment, ObservableSource<out List<Event>>> { enrollment: Enrollment ->
                            d2
                                .eventModule()
                                .events()
                                .byEnrollmentUid()
                                .eq(enrollment.uid())
                                .get()
                                .toObservable()
                        },
                    )
                },
            ).flatMap(
                Function<List<Event>, ObservableSource<out Boolean>> { data: List<Event>? ->
                    Observable.fromIterable(data).flatMap(
                        Function<Event, ObservableSource<out Boolean>> { event: Event ->
                            bitmaps.add(QrViewModel(QRjson.EVENTS_JSON, gson.toJson(event)))
                            d2
                                .trackedEntityModule()
                                .trackedEntityDataValues()
                                .byEvent()
                                .eq(event.uid())
                                .get()
                                .toObservable()
                                .map(
                                    Function { dataValueList: List<TrackedEntityDataValue> ->
                                        val arrayListAux = ArrayList<TrackedEntityDataValue>()
                                        // DIVIDE ATTR QR GENERATION -> 1 QR PER 2 ATTR
                                        var count = 0
                                        for (i in dataValueList.indices) {
                                            arrayListAux.add(dataValueList[i])
                                            if (count == 1) {
                                                count = 0
                                                bitmaps.add(
                                                    QrViewModel(
                                                        QRjson.DATA_JSON,
                                                        gson.toJson(arrayListAux),
                                                    ),
                                                )
                                                arrayListAux.clear()
                                            } else if (i == dataValueList.size - 1) {
                                                bitmaps.add(
                                                    QrViewModel(
                                                        QRjson.DATA_JSON,
                                                        gson.toJson(arrayListAux),
                                                    ),
                                                )
                                            } else {
                                                count++
                                            }
                                        }
                                        true
                                    },
                                )
                        },
                    )
                },
            ).map(Function<Boolean, List<QrViewModel>> { _: Boolean? -> bitmaps })
    }

    override fun eventWORegistrationQRs(eventUid: String): Observable<List<QrViewModel>> {
        val bitmaps: MutableList<QrViewModel> = ArrayList()

        return d2
            .eventModule()
            .events()
            .uid(eventUid)
            .get()
            .map<Event>(
                Function { data: Event ->
                    bitmaps.add(QrViewModel(QRjson.EVENT_JSON, gson.toJson(data)))
                    data
                },
            ).flatMap(
                Function<Event, SingleSource<out List<TrackedEntityDataValue>>> { data: Event ->
                    d2
                        .trackedEntityModule()
                        .trackedEntityDataValues()
                        .byEvent()
                        .eq(data.uid())
                        .get()
                },
            ).map(
                Function { data: List<TrackedEntityDataValue> ->
                    val arrayListAux = ArrayList<TrackedEntityDataValue>()
                    // DIVIDE ATTR QR GENERATION -> 1 QR PER 2 ATTR
                    var count = 0
                    for (i in data.indices) {
                        arrayListAux.add(data[i])
                        if (count == 1) {
                            count = 0
                            bitmaps.add(
                                QrViewModel(
                                    QRjson.DATA_JSON_WO_REGISTRATION,
                                    gson.toJson(arrayListAux),
                                ),
                            )
                            arrayListAux.clear()
                        } else if (i == data.size - 1) {
                            bitmaps.add(
                                QrViewModel(
                                    QRjson.DATA_JSON_WO_REGISTRATION,
                                    gson.toJson(arrayListAux),
                                ),
                            )
                        } else {
                            count++
                        }
                    }
                    true
                },
            ).toObservable()
            .map(Function<Boolean, List<QrViewModel>> { _: Boolean? -> bitmaps })
    }

    private fun decodeData(data: String): String {
        val decodedBytes = Base64.decode(data, Base64.DEFAULT)
        return String(decodedBytes, StandardCharsets.UTF_8)
    }

    override fun getUncodedData(teiUid: String): Observable<Bitmap> =
        Observable
            .fromCallable(Callable { getData(teiUid) })
            .map(Function { dataToCompress: String -> this.compress(dataToCompress) })
            .map(Function { inputData: ByteArray? -> transform(inputData) })

    override fun getNFCData(teiUid: String): Observable<ByteArray> =
        Observable
            .fromCallable(Callable { getData(teiUid) })
            .map(Function { dataToCompress: String -> this.compress(dataToCompress) })

    override fun setData(inputData: String): Observable<Boolean> =
        Observable
            .fromCallable(Callable { decompress(decodeData(inputData).toByteArray()) })
            .map(Function { _: String -> true })

    fun compress(dataToCompress: String): ByteArray {
        val input = dataToCompress.toByteArray(StandardCharsets.UTF_8)
        val compressor = Deflater(Deflater.BEST_COMPRESSION)
        compressor.setInput(input)
        compressor.finish()
        val baos = ByteArrayOutputStream()
        val buf = ByteArray(8192)
        while (!compressor.finished()) {
            val byteCount = compressor.deflate(buf)
            baos.write(buf, 0, byteCount)
        }
        compressor.end()
        return baos.toByteArray()
    }

    override fun decompress(dataToDecompress: ByteArray?): String {
        if (dataToDecompress == null) return "DATA WAS NULL"
        try {
            val decompresser = Inflater()
            decompresser.setInput(dataToDecompress)
            val outputStream = ByteArrayOutputStream()
            val buffer = ByteArray(100)
            while (decompresser.inflate(buffer) != 0) {
                outputStream.write(buffer)
            }
            decompresser.end()
            outputStream.close()
            val output = outputStream.toByteArray()
            return String(output, StandardCharsets.UTF_8)
        } catch (e: DataFormatException) {
            Timber.e(e)
            return ""
        } catch (e: IOException) {
            Timber.e(e)
            return ""
        }
    }

    /**
     * BUILD DATA STRING
     */
    private fun getData(teiUid: String): String {
        val dataBuilder: java.lang.StringBuilder = StringBuilder()
        dataBuilder.append(TEI_FLAG)
        val tei =
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .uid(teiUid)
                .blockingGet()
        dataBuilder.append(setTeiData(tei!!))
        val enrollments =
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance()
                .eq(teiUid)
                .blockingGet()
        for (enrollment in enrollments) {
            dataBuilder.append(ENROLLMENT_FLAG)
            dataBuilder.append(setEnrollmentData(enrollment))
            val teAttrValues =
                d2
                    .trackedEntityModule()
                    .trackedEntityAttributeValues()
                    .byTrackedEntityInstance()
                    .eq(teiUid)
                    .blockingGet()
            for (attrValue in teAttrValues) {
                dataBuilder.append(ATTR_FLAG)
                dataBuilder.append(setAttrData(attrValue))
            }
            val events =
                d2
                    .eventModule()
                    .events()
                    .byEnrollmentUid()
                    .eq(enrollment.uid())
                    .blockingGet()
            for (event in events) {
                dataBuilder.append(EVENT_FLAG)
                dataBuilder.append(setEventData(event))
                val teDataValue =
                    d2
                        .trackedEntityModule()
                        .trackedEntityDataValues()
                        .byEvent()
                        .eq(event.uid())
                        .blockingGet()
                for (dataValue in teDataValue) {
                    dataBuilder.append(DE_FLAG)
                    dataBuilder.append(setTEDataValue(dataValue))
                }
            }
        }

        return dataBuilder.toString()
    }

    private fun setAttrData(attrValue: TrackedEntityAttributeValue): String {
        val data: MutableList<String?> = ArrayList()
        data.add(attrValue.trackedEntityAttribute())
        data.add(attrValue.value())
        return TextUtils.join("|", data)
    }

    private fun setTEDataValue(dataValue: TrackedEntityDataValue): String {
        val data: MutableList<String?> = ArrayList()
        data.add(dataValue.dataElement())
        data.add(dataValue.value())
        return TextUtils.join("|", data)
    }

    private fun setEventData(event: Event): String {
        val data: MutableList<String?> = ArrayList()
        data.add(event.uid())
        data.add(DateUtils.databaseDateFormat().format(event.created()))
        data.add(event.status()!!.name)

        /*  data.add(event.coordinate() != null ? String.valueOf(event.coordinate().latitude()) : "");
        data.add(event.coordinate() != null ? String.valueOf(event.coordinate().longitude()) : "");*/

        data.add(event.program()) // TEI OR ENROLLMENT?
        data.add(event.programStage())
        data.add(
            if (event.eventDate() != null) {
                DateUtils.databaseDateFormat().format(event.eventDate())
            } else {
                ""
            },
        )
        data.add(
            if (event.completedDate() != null) {
                DateUtils.databaseDateFormat().format(event.completedDate())
            } else {
                ""
            },
        )
        data.add(
            if (event.dueDate() != null) {
                DateUtils.databaseDateFormat().format(event.created())
            } else {
                ""
            },
        )
        data.add(if (event.aggregatedSyncState() != null) event.aggregatedSyncState()?.name else "")
        data.add(event.organisationUnit())

        return TextUtils.join("|", data)
    }

    private fun setTeiData(tei: TrackedEntityInstance): String {
        val data: MutableList<String?> = ArrayList()
        data.add(tei.uid())
        data.add(DateUtils.databaseDateFormat().format(tei.created()))
        data.add(tei.organisationUnit())
        data.add(tei.trackedEntityType())

        data.add(if (tei.aggregatedSyncState() != null) tei.aggregatedSyncState()!!.name else "")
        return TextUtils.join("|", data)
    }

    private fun setEnrollmentData(enrollment: Enrollment): String {
        val data: MutableList<String?> = ArrayList()
        data.add(enrollment.uid())
        data.add(DateUtils.databaseDateFormat().format(enrollment.created()))
        data.add(enrollment.organisationUnit())
        data.add(enrollment.program())
        data.add(
            if (enrollment.enrollmentDate() != null) {
                DateUtils.databaseDateFormat().format(enrollment.enrollmentDate())
            } else {
                ""
            },
        )
        data.add(
            if (enrollment.incidentDate() != null) {
                DateUtils.databaseDateFormat().format(enrollment.incidentDate())
            } else {
                ""
            },
        )
        data.add(if (enrollment.followUp()!!) "t" else "f")
        data.add(enrollment.status()!!.name)

        data.add(enrollment.aggregatedSyncState()!!.name)
        return TextUtils.join("|", data)
    }

    /**
     * BUILD DATA FROM STRING
     */
    override fun saveData(data: String): String {
        val teiData = data.split("\\\$T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val enrollment =
            teiData[1].split("\\\$R".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val tei = saveTeiData(enrollment[0])
        for (i in 1..<enrollment.size) {
            val attributes =
                enrollment[i]
                    .split("\\\$A|\\\$E".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val events =
                enrollment[i].split("\\\$E".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val enrollmentModel =
                saveEnrollmentData(tei, if (attributes.size != 0) attributes[0] else events[0])

            for (attr in 1..<attributes.size) {
                saveAttribute(tei, attributes[attr])
            }

            for (ev in 1..<events.size) {
                val dataElements =
                    events[ev]
                        .split("\\\$D".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                val event = saveEvent(enrollmentModel, dataElements[0])
                for (de in 1..<dataElements.size) {
                    saveDataElement(event, dataElements[de])
                }
            }
        }

        return tei.uid()
    }

    private fun saveTeiData(teiData: String): TrackedEntityInstance {
        val data = teiData.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var created: Date? = null
        try {
            created = DateUtils.databaseDateFormat().parse(data[1])
        } catch (e: ParseException) {
            Timber.e(e)
        }
        val tei =
            TrackedEntityInstance
                .builder()
                .uid(data[0])
                .created(created ?: Calendar.getInstance().time)
                .lastUpdated(Calendar.getInstance().time)
                .organisationUnit(data[2])
                .trackedEntityType(data[3])
                .geometry(
                    if (data[5].isEmpty()) {
                        null
                    } else {
                        // TODO: CHANGE TO SUPPORT ALL FEATURE TYPES
                        Geometry
                            .builder()
                            .type(FeatureType.POINT)
                            .coordinates(data[5])
                            .build()
                    },
                ).aggregatedSyncState(State.valueOf(data[6]))
                .build()

        runBlocking { d2.databaseAdapter().upsertObject(tei, TrackedEntityInstance::class) }
        return tei
    }

    private fun saveEnrollmentData(
        tei: TrackedEntityInstance,
        enrollmentData: String,
    ): Enrollment {
        val data =
            enrollmentData.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var created: Date? = null
        var enrollmentDate: Date? = null
        var incidentDate: Date? = null
        try {
            created = DateUtils.databaseDateFormat().parse(data[1])
        } catch (e: ParseException) {
            Timber.e(e)
        }
        try {
            enrollmentDate = DateUtils.databaseDateFormat().parse(data[1])
        } catch (e: ParseException) {
            Timber.e(e)
        }

        if (data[5].isNotEmpty()) {
            try {
                incidentDate = DateUtils.databaseDateFormat().parse(data[5])
            } catch (e: ParseException) {
                Timber.e(e)
            }
        }

        val enrollment =
            Enrollment
                .builder()
                .uid(data[0])
                .created(created ?: Calendar.getInstance().time)
                .lastUpdated(Calendar.getInstance().time)
                .organisationUnit(data[2])
                .program(data[3])
                .enrollmentDate(enrollmentDate)
                .incidentDate(incidentDate)
                .followUp(data[6] == "t")
                .status(EnrollmentStatus.valueOf(data[7]))
                .geometry(
                    if (data[8].isEmpty()) {
                        null
                    } else {
                        Geometry
                            .builder()
                            // TODO: CHANGE TO SUPPORT ALL FEATURE TYPES
                            .type(FeatureType.POINT)
                            .coordinates(
                                Coordinates
                                    .create(data[8].toDouble(), data[9].toDouble())
                                    .toString(),
                            ).build()
                    },
                ).aggregatedSyncState(State.valueOf(data[10]))
                .trackedEntityInstance(tei.uid())
                .build()

        runBlocking { d2.databaseAdapter().upsertObject(enrollment, Enrollment::class) }
        return enrollment
    }

    fun saveAttribute(
        tei: TrackedEntityInstance,
        attrData: String,
    ) {
        val data = attrData.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val attribute =
            TrackedEntityAttributeValue
                .builder()
                .created(Calendar.getInstance().time)
                .lastUpdated(Calendar.getInstance().time)
                .trackedEntityAttribute(data[0])
                .trackedEntityInstance(tei.uid())
                .value(data[1])
                .build()

        runBlocking {
            d2.databaseAdapter().upsertObject(attribute, TrackedEntityAttributeValue::class)
        }
    }

    fun saveEvent(
        enrollment: Enrollment,
        eventData: String,
    ): Event {
        val data = eventData.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var created: Date? = null
        var eventDate: Date? = null
        var completeDate: Date? = null
        var dueDate: Date? = null
        try {
            created = DateUtils.databaseDateFormat().parse(data[1])
        } catch (e: ParseException) {
            Timber.e(e)
        }
        if (data[7].isNotEmpty()) {
            try {
                eventDate = DateUtils.databaseDateFormat().parse(data[7])
            } catch (e: ParseException) {
                Timber.e(e)
            }
        }
        if (data[8].isNotEmpty()) {
            try {
                completeDate = DateUtils.databaseDateFormat().parse(data[8])
            } catch (e: ParseException) {
                Timber.e(e)
            }
        }
        if (data[9].isNotEmpty()) {
            try {
                dueDate = DateUtils.databaseDateFormat().parse(data[9])
            } catch (e: ParseException) {
                Timber.e(e)
            }
        }
        val event =
            Event
                .builder()
                .uid(data[0])
                .created(created)
                .status(EventStatus.valueOf(data[2]))
                .geometry(
                    if (data[3].isEmpty()) {
                        null
                    } else {
                        Geometry
                            .builder() // TODO: CHANGE TO SUPPORT ALL FEATURE TYPES
                            .type(FeatureType.POINT)
                            .coordinates(
                                Coordinates
                                    .create(data[3].toDouble(), data[4].toDouble())
                                    .toString(),
                            ).build()
                    },
                ).organisationUnit(data[11])
                .program(data[5])
                .programStage(data[6])
                .eventDate(eventDate)
                .completedDate(completeDate)
                .dueDate(dueDate)
                .enrollment(enrollment.uid())
                .lastUpdated(Calendar.getInstance().time)
                .aggregatedSyncState(State.valueOf(data[10]))
                .build()

        runBlocking { d2.databaseAdapter().upsertObject(event, Event::class) }
        return event
    }

    fun saveDataElement(
        event: Event,
        deData: String,
    ) {
        val data = deData.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val dataValue =
            TrackedEntityDataValue
                .builder()
                .created(Calendar.getInstance().time)
                .dataElement(data[0])
                .value(data[1])
                .event(event.uid())
                .build()
        runBlocking { d2.databaseAdapter().upsertObject(dataValue, TrackedEntityDataValue::class) }
    }

    companion object {
        private const val TEI_FLAG = "\$T"
        private const val ENROLLMENT_FLAG = "\$R"
        private const val EVENT_FLAG = "\$E"
        private const val ATTR_FLAG = "\$A"
        private const val DE_FLAG = "\$D"

        private val TEI_PATTERN: Pattern = Pattern.compile("\\\$T(.+)")

        @JvmStatic
        fun transform(
            type: String?,
            info: String,
        ): Bitmap? {
            val data: ByteArray
            var encoded: String?
            try {
                data = info.toByteArray(charset("UTF-8"))
                encoded = Base64.encodeToString(data, Base64.DEFAULT)
            } catch (e: UnsupportedEncodingException) {
                Timber.e(e)
                encoded = e.localizedMessage
            }

            val multiFormatWriter = MultiFormatWriter()
            var bitmap: Bitmap? = null
            val gson = GsonBuilder().setDateFormat(DateUtils.DATABASE_FORMAT_EXPRESSION).create()
            try {
                val bitMatrix =
                    multiFormatWriter.encode(
                        gson.toJson(QRjson(type, encoded)),
                        BarcodeFormat.QR_CODE,
                        1000,
                        1000,
                    )
                val barcodeEncoder = BarcodeEncoder()
                bitmap = barcodeEncoder.createBitmap(bitMatrix)
            } catch (e: WriterException) {
                Timber.e(e)
            }

            return bitmap
        }

        fun transform(inputData: ByteArray?): Bitmap? {
            val encoded = Base64.encodeToString(inputData, Base64.DEFAULT)

            val multiFormatWriter = MultiFormatWriter()
            var bitmap: Bitmap? = null
            try {
                val bitMatrix = multiFormatWriter.encode(encoded, BarcodeFormat.QR_CODE, 1000, 1000)
                val barcodeEncoder = BarcodeEncoder()
                bitmap = barcodeEncoder.createBitmap(bitMatrix)
            } catch (e: WriterException) {
                Timber.e(e)
            }

            return bitmap
        }
    }
}

private fun divideEnrollmentsInQrGeneration(
    bitmaps: MutableList<QrViewModel>,
    data: List<Enrollment>,
): MutableList<QrViewModel> {
    val arrayListAux = ArrayList<Enrollment>()
    var count = 0
    for (i in data.indices) {
        arrayListAux.add(data[i])
        if (count == 1) {
            count = 0
            bitmaps.add(QrViewModel(QRjson.ENROLLMENT_JSON, Gson().toJson(arrayListAux)))
            arrayListAux.clear()
        } else if (i == data.size - 1) {
            bitmaps.add(QrViewModel(QRjson.ENROLLMENT_JSON, Gson().toJson(arrayListAux)))
        } else {
            count++
        }
    }
    return bitmaps
}

private fun divideAttributesInQrGeneration(
    bitmaps: MutableList<QrViewModel>,
    data: List<TrackedEntityAttributeValue>,
): MutableList<QrViewModel> {
    val arrayListAux = ArrayList<TrackedEntityAttributeValue>()

    var count = 0
    for (i in data.indices) {
        arrayListAux.add(data[i])
        if (count == 1) {
            count = 0
            bitmaps.add(QrViewModel(QRjson.ATTR_JSON, Gson().toJson(arrayListAux)))
            arrayListAux.clear()
        } else if (i == data.size - 1) {
            bitmaps.add(QrViewModel(QRjson.ATTR_JSON, Gson().toJson(arrayListAux)))
        } else {
            count++
        }
    }
    return bitmaps
}

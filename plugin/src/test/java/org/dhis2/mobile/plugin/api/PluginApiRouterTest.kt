package org.dhis2.mobile.plugin.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.category.CategoryOptionComboCollectionRepository
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.datastore.DataStoreCollectionRepository
import org.hisp.dhis.android.core.datastore.DataStoreEntry
import org.hisp.dhis.android.core.datavalue.DataValueObjectRepository
import org.hisp.dhis.android.core.user.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PluginApiRouterTest {
    private val d2: D2 = mock(defaultAnswer = RETURNS_DEEP_STUBS)
    private val unconfined = Dispatchers.Unconfined
    private val router =
        PluginApiRouter(
            d2 = d2,
            dispatcher = Dispatcher(io = unconfined, main = unconfined, default = unconfined),
        )

    @Test
    fun `strips the api version and serves the current user from the SDK`() =
        runTest {
            val user: User =
                mock {
                    on { uid() } doReturn "userUid"
                    on { username() } doReturn "android"
                    on { displayName() } doReturn "Android User"
                }
            whenever(d2.userModule().user().blockingGet()) doReturn user

            val response = router.handle(ApiRequest("GET", "/api/41/me?fields=id,username", null))

            assertEquals(200, response.status)
            val body = response.json()
            assertEquals("userUid", body["id"]!!.jsonPrimitive.content)
            assertEquals("android", body["username"]!!.jsonPrimitive.content)
        }

    @Test
    fun `serves system info with the server version the SDK recorded`() =
        runTest {
            whenever(
                d2
                    .systemInfoModule()
                    .systemInfo()
                    .blockingGet()
                    ?.version(),
            ) doReturn "2.41.3"

            val response = router.handle(ApiRequest("GET", "/api/system/info", null))

            assertEquals(200, response.status)
            assertEquals("2.41.3", response.json()["version"]!!.jsonPrimitive.content)
        }

    @Test
    fun `reads a dataStore value from the local dataStore`() =
        runTest {
            whenever(
                d2
                    .dataStoreModule()
                    .dataStore()
                    .value("ns", "key")
                    .blockingGet(),
            ) doReturn
                DataStoreEntry("ns", "key", """{"count":3}""", State.SYNCED, false)

            val response = router.handle(ApiRequest("GET", "/api/dataStore/ns/key", null))

            assertEquals(200, response.status)
            assertEquals(3, response.json()["count"]!!.jsonPrimitive.int)
        }

    @Test
    fun `answers 404 for a dataStore key that does not exist locally`() =
        runTest {
            whenever(
                d2
                    .dataStoreModule()
                    .dataStore()
                    .value("ns", "missing")
                    .blockingGet(),
            ) doReturn null

            val response = router.handle(ApiRequest("GET", "/api/dataStore/ns/missing", null))

            assertEquals(404, response.status)
        }

    @Test
    fun `answers 404 for a dataStore key deleted locally but not yet synced`() =
        runTest {
            whenever(
                d2
                    .dataStoreModule()
                    .dataStore()
                    .value("ns", "gone")
                    .blockingGet(),
            ) doReturn
                DataStoreEntry("ns", "gone", "{}", State.TO_UPDATE, true)

            val response = router.handle(ApiRequest("GET", "/api/dataStore/ns/gone", null))

            assertEquals(404, response.status)
        }

    @Test
    fun `lists the keys of a namespace`() =
        runTest {
            // Filter connectors are generic in their repository, which deep stubs erase; stub the chain.
            val namespaceFilter: StringFilterConnector<DataStoreCollectionRepository> = mock()
            val filtered: DataStoreCollectionRepository = mock()
            whenever(d2.dataStoreModule().dataStore().byNamespace()) doReturn namespaceFilter
            whenever(namespaceFilter.eq("ns")) doReturn filtered
            whenever(filtered.blockingGet()) doReturn
                listOf(
                    DataStoreEntry("ns", "a", "1", State.SYNCED, false),
                    DataStoreEntry("ns", "b", "2", State.TO_POST, false),
                    DataStoreEntry("ns", "c", "3", State.TO_UPDATE, true),
                )

            val response = router.handle(ApiRequest("GET", "/api/dataStore/ns", null))

            assertEquals(200, response.status)
            assertEquals(
                listOf("a", "b"),
                Json.parseToJsonElement(response.body).jsonArray.map { it.jsonPrimitive.content },
            )
        }

    @Test
    fun `writes a dataStore value through the SDK so it syncs later`() =
        runTest {
            val response = router.handle(ApiRequest("PUT", "/api/41/dataStore/ns/key", """{"count":4}"""))

            assertEquals(200, response.status)
            verify(d2.dataStoreModule().dataStore().value("ns", "key")).blockingSet("""{"count":4}""")
        }

    @Test
    fun `creating a dataStore key that already exists is a conflict`() =
        runTest {
            whenever(
                d2
                    .dataStoreModule()
                    .dataStore()
                    .value("ns", "key")
                    .blockingGet(),
            ) doReturn
                DataStoreEntry("ns", "key", "{}", State.SYNCED, false)

            val response = router.handle(ApiRequest("POST", "/api/dataStore/ns/key", "{}"))

            assertEquals(409, response.status)
            verify(d2.dataStoreModule().dataStore().value("ns", "key"), never()).blockingSet(any())
        }

    @Test
    fun `creating a new dataStore key answers 201`() =
        runTest {
            whenever(
                d2
                    .dataStoreModule()
                    .dataStore()
                    .value("ns", "new")
                    .blockingGet(),
            ) doReturn null

            val response = router.handle(ApiRequest("POST", "/api/dataStore/ns/new", """{"a":1}"""))

            assertEquals(201, response.status)
            verify(d2.dataStoreModule().dataStore().value("ns", "new")).blockingSet("""{"a":1}""")
        }

    @Test
    fun `imports a data value set into the local database`() =
        runTest {
            val repository: DataValueObjectRepository = mock()
            whenever(
                d2.dataValueModule().dataValues().value("202601", "orgUnit", "de1", "coc1", "aoc1", "ds1"),
            ) doReturn repository

            val response =
                router.handle(
                    ApiRequest(
                        "POST",
                        "/api/dataValueSets",
                        """
                        {
                          "dataSet": "ds1",
                          "period": "202601",
                          "orgUnit": "orgUnit",
                          "attributeOptionCombo": "aoc1",
                          "dataValues": [
                            {"dataElement": "de1", "categoryOptionCombo": "coc1", "value": "12"}
                          ]
                        }
                        """.trimIndent(),
                    ),
                )

            assertEquals(200, response.status)
            verify(repository).blockingSet("12")
            val importCount = response.json()["response"]!!.jsonObject["importCount"]!!.jsonObject
            assertEquals(1, importCount["imported"]!!.jsonPrimitive.int)
        }

    /** The data element's and the data set's category combos, and that combo's only option combo. */
    private fun givenCategoryCombos(
        dataElementCombo: Pair<String, Boolean>,
        dataSetCombo: Pair<String, Boolean>,
        defaultOptionCombo: String,
    ) {
        val dataElement: DataElement = mock { on { categoryCombo() } doReturn ObjectWithUid.create(dataElementCombo.first) }
        val dataElementRepository: ReadOnlyOneObjectRepositoryFinalImpl<DataElement> = mock { on { blockingGet() } doReturn dataElement }
        whenever(d2.dataElementModule().dataElements().uid("de1")) doReturn dataElementRepository

        val dataSet: DataSet = mock { on { categoryCombo() } doReturn ObjectWithUid.create(dataSetCombo.first) }
        val dataSetRepository: ReadOnlyOneObjectRepositoryFinalImpl<DataSet> = mock { on { blockingGet() } doReturn dataSet }
        whenever(d2.dataSetModule().dataSets().uid("ds1")) doReturn dataSetRepository

        listOf(dataElementCombo, dataSetCombo).distinct().forEach { (comboUid, isDefault) ->
            val combo: CategoryCombo = mock { on { this.isDefault } doReturn isDefault }
            val comboRepository: ReadOnlyOneObjectRepositoryFinalImpl<CategoryCombo> = mock { on { blockingGet() } doReturn combo }
            whenever(d2.categoryModule().categoryCombos().uid(comboUid)) doReturn comboRepository
        }

        val byCombo: StringFilterConnector<CategoryOptionComboCollectionRepository> = mock()
        val optionCombos: CategoryOptionComboCollectionRepository = mock()
        val oneOptionCombo: ReadOnlyOneObjectRepositoryFinalImpl<CategoryOptionCombo> = mock()
        val optionCombo: CategoryOptionCombo = mock { on { uid() } doReturn defaultOptionCombo }
        whenever(d2.categoryModule().categoryOptionCombos().byCategoryComboUid()) doReturn byCombo
        whenever(byCombo.eq(any())) doReturn optionCombos
        whenever(optionCombos.one()) doReturn oneOptionCombo
        whenever(oneOptionCombo.blockingGet()) doReturn optionCombo
    }

    @Test
    fun `missing option combos are filled from default category combos`() =
        runTest {
            givenCategoryCombos(
                dataElementCombo = "defaultCombo" to true,
                dataSetCombo = "defaultCombo" to true,
                defaultOptionCombo = "defaultCoc",
            )
            val repository: DataValueObjectRepository = mock()
            whenever(
                d2.dataValueModule().dataValues().value("202601", "orgUnit", "de1", "defaultCoc", "defaultCoc", "ds1"),
            ) doReturn repository

            router.handle(
                ApiRequest(
                    "POST",
                    "/api/dataValueSets",
                    """{"dataSet":"ds1","period":"202601","orgUnit":"orgUnit","dataValues":[{"dataElement":"de1","value":"5"}]}""",
                ),
            )

            verify(repository).blockingSet("5")
        }

    @Test
    fun `a missing attribute option combo is rejected when the data set has an attribute combo`() =
        runTest {
            // The server rejects such a value (E8023). Storing it would leave data on the device
            // that can never sync, so it is refused now, while the plugin can still tell the user.
            givenCategoryCombos(
                dataElementCombo = "defaultCombo" to true,
                dataSetCombo = "fundingMechanism" to false,
                defaultOptionCombo = "defaultCoc",
            )

            val response =
                router.handle(
                    ApiRequest(
                        "POST",
                        "/api/dataValueSets",
                        """{"dataSet":"ds1","period":"202601","orgUnit":"orgUnit","dataValues":[{"dataElement":"de1","value":"5"}]}""",
                    ),
                )

            assertEquals(409, response.status)
            verify(d2.dataValueModule().dataValues(), never()).value(any(), any(), any(), any(), any(), any())
            val importCount = response.json()["response"]!!.jsonObject["importCount"]!!.jsonObject
            assertEquals(1, importCount["ignored"]!!.jsonPrimitive.int)
            // app-runtime shows `message` for a 409, so it must carry the reason.
            assertTrue(
                response
                    .json()["message"]!!
                    .jsonPrimitive.content
                    .contains("attributeOptionCombo"),
            )
        }

    @Test
    fun `a data value set without its data set is rejected rather than guessed`() =
        runTest {
            val response =
                router.handle(
                    ApiRequest(
                        "POST",
                        "/api/dataValueSets",
                        """{"period":"202601","orgUnit":"orgUnit","dataValues":[{"dataElement":"de1","value":"5"}]}""",
                    ),
                )

            assertEquals(409, response.status)
        }

    @Test
    fun `an endpoint without an offline implementation answers 501`() =
        runTest {
            val response = router.handle(ApiRequest("GET", "/api/analytics?dimension=dx:abc", null))

            assertEquals(501, response.status)
        }

    @Test
    fun `an SDK failure becomes a 500 instead of escaping to the WebView`() =
        runTest {
            whenever(
                d2
                    .dataStoreModule()
                    .dataStore()
                    .value("ns", "key")
                    .blockingGet(),
            ) doThrow
                RuntimeException("database closed")

            val response = router.handle(ApiRequest("GET", "/api/dataStore/ns/key", null))

            assertEquals(500, response.status)
        }

    private fun ApiResponse.json(): JsonObject = Json.parseToJsonElement(body).jsonObject
}

package org.dhis2.usescases.searchTrackEntity

import kotlinx.coroutines.Dispatchers
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeCollectionRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SearchRepositoryTest {

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val fieldViewModelFactory: FieldViewModelFactory = mock()
    private val metadataIconProvider: MetadataIconProvider = mock()
    private val dispatchers: DispatcherProvider = mock {
        on { io() } doReturn Dispatchers.IO
    }
    private lateinit var searchRepository: SearchRepositoryImplKt

    @Before
    fun setUp() {
        val trackedEntityAttributes = mapOf(
            "unique-code" to createTrackedEntityAttributeRepository("unique-code", true),
            "bp-number" to createTrackedEntityAttributeRepository("bp-number", true),
            "national-id" to createTrackedEntityAttributeRepository("national-id", true),
            "unique-id" to createTrackedEntityAttributeRepository("unique-id", true),
        )

        val trackedEntityAttributeCollection = mock<TrackedEntityAttributeCollectionRepository>()
        whenever(d2.trackedEntityModule().trackedEntityAttributes()).thenReturn(trackedEntityAttributeCollection)
        whenever(trackedEntityAttributeCollection.uid(anyString())).thenAnswer { invocation ->
            val uid = invocation.arguments[0] as String
            trackedEntityAttributes[uid] ?: createTrackedEntityAttributeRepository(uid, false)
        }

        searchRepository = SearchRepositoryImplKt(
            searchRepositoryJava = mock(),
            d2 = d2,
            dispatcher = dispatchers,
            fieldViewModelFactory = fieldViewModelFactory,
            metadataIconProvider = metadataIconProvider,
            trackedEntityInstanceInfoProvider = mock(),
            eventInfoProvider = mock(),
        )
    }

    @Test
    fun shouldSortSearchParametersCorrectly() {
        val mockData = createMockData()
        val sortedData = searchRepository.sortSearchParameters(mockData)

        assertEquals("unique-code", sortedData[0].uid)
        assertEquals("bp-number", sortedData[1].uid)
        assertEquals("qr-code", sortedData[2].uid)
        assertEquals("bar-code", sortedData[3].uid)
        assertEquals("unique-id", sortedData[4].uid)
        assertEquals("national-id", sortedData[5].uid)
        assertEquals("first-name", sortedData[6].uid)
        assertEquals("last-name", sortedData[7].uid)
        assertEquals("phone-number", sortedData[8].uid)
        assertEquals("state", sortedData[9].uid)
    }

    private fun createTrackedEntityAttributeRepository(
        uid: String,
        unique: Boolean,
    ): ReadOnlyOneObjectRepositoryFinalImpl<TrackedEntityAttribute> {
        val attribute = mock<TrackedEntityAttribute> {
            on { uid() } doReturn uid
            on { unique() } doReturn unique
        }
        return mock {
            on { blockingGet() } doReturn attribute
        }
    }

    private fun createMockData(): List<FieldUiModel> {
        return listOf(
            FieldUiModelImpl(
                uid = "first-name",
                label = "First Name",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.DEFAULT,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "unique-code",
                label = "Unique Code",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.QR_CODE,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "last-name",
                label = "Last Name",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.DEFAULT,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "phone-number",
                label = "Phone Number",
                valueType = ValueType.PHONE_NUMBER,
                renderingType = UiRenderType.DEFAULT,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "unique-id",
                label = "Unique ID",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.DEFAULT,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "state",
                label = "State",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.DEFAULT,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "national-id",
                label = "National ID",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.DEFAULT,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "qr-code",
                label = "qr-code",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.QR_CODE,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "bar-code",
                label = "bar-code",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.BAR_CODE,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
            FieldUiModelImpl(
                uid = "bp-number",
                label = "BP Number",
                valueType = ValueType.TEXT,
                renderingType = UiRenderType.BAR_CODE,
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
            ),
        )
    }
}

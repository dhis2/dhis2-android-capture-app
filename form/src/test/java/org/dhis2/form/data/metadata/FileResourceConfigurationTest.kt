package org.dhis2.form.data.metadata

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.fileresource.FileResource
import org.hisp.dhis.android.core.fileresource.FileResourceCollectionRepository
import org.hisp.dhis.android.core.fileresource.FileResourceModule
import org.hisp.dhis.android.core.fileresource.FileResourceObjectRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FileResourceConfigurationTest {
    private val d2 = mock<D2>()
    private val fileResourceModule = mock<FileResourceModule>()
    private val fileResources: FileResourceCollectionRepository = mock()
    private val fileResourceObjectRepository: FileResourceObjectRepository = mock()
    private val fileResource = mock<FileResource>()

    private val byUidConnector: StringFilterConnector<FileResourceCollectionRepository> = mock()
    private val byPathConnector: StringFilterConnector<FileResourceCollectionRepository> = mock()
    private val byUidFiltered: FileResourceCollectionRepository = mock()
    private val byPathFiltered: FileResourceCollectionRepository = mock()
    private val byUidOne: ReadOnlyOneObjectRepositoryFinalImpl<FileResource> = mock()
    private val byPathOne: ReadOnlyOneObjectRepositoryFinalImpl<FileResource> = mock()

    private lateinit var fileResourceConfiguration: FileResourceConfiguration

    @Before
    fun setUp() {
        fileResourceConfiguration = FileResourceConfiguration(d2)

        whenever(d2.fileResourceModule()).thenReturn(fileResourceModule)
        whenever(fileResourceModule.fileResources()).thenReturn(fileResources)
        whenever(fileResources.uid(anyString())).thenReturn(fileResourceObjectRepository)

        whenever(fileResources.byUid()).thenReturn(byUidConnector)
        whenever(fileResources.byPath()).thenReturn(byPathConnector)
        whenever(byUidConnector.eq(anyString())).thenReturn(byUidFiltered)
        whenever(byPathConnector.eq(anyString())).thenReturn(byPathFiltered)
        whenever(byUidFiltered.one()).thenReturn(byUidOne)
        whenever(byPathFiltered.one()).thenReturn(byPathOne)
    }

    @Test
    fun `test getFilePath with existing UID`() {
        val uid = "existing_uid"
        val expectedPath = "/path/to/file"

        whenever(fileResourceObjectRepository.blockingExists()).thenReturn(true)
        whenever(fileResourceObjectRepository.blockingGet()).thenReturn(fileResource)
        whenever(fileResource.path()).thenReturn(expectedPath)

        val result = fileResourceConfiguration.getFilePath(uid)

        assertEquals(expectedPath, result)
    }

    @Test
    fun `test getFilePath with non-existing UID`() {
        val uid = "non_existing_uid"

        whenever(fileResourceObjectRepository.blockingExists()).thenReturn(false)

        val result = fileResourceConfiguration.getFilePath(uid)

        assertNull(result)
    }

    @Test
    fun `should return the original file name when the value is the file resource uid`() {
        whenever(byUidOne.blockingGet()).thenReturn(fileResource)
        whenever(fileResource.name()).thenReturn("report.pdf")

        val result = fileResourceConfiguration.getFileName("existing_uid")

        assertEquals("report.pdf", result)
    }

    @Test
    fun `should return the original file name when the value is the file path`() {
        whenever(byUidOne.blockingGet()).thenReturn(null)
        whenever(byPathOne.blockingGet()).thenReturn(fileResource)
        whenever(fileResource.name()).thenReturn("report.pdf")

        val result = fileResourceConfiguration.getFileName("/sdk_resources/db/existing_uid.pdf")

        assertEquals("report.pdf", result)
    }

    @Test
    fun `should return null when there is no file resource for the value`() {
        whenever(byUidOne.blockingGet()).thenReturn(null)
        whenever(byPathOne.blockingGet()).thenReturn(null)

        val result = fileResourceConfiguration.getFileName("missing")

        assertNull(result)
    }

    @Test
    fun `should return null when the file resource has no name`() {
        whenever(byUidOne.blockingGet()).thenReturn(fileResource)
        whenever(byPathOne.blockingGet()).thenReturn(fileResource)
        whenever(fileResource.name()).thenReturn(null)

        val result = fileResourceConfiguration.getFileName("existing_uid")

        assertNull(result)
    }
}

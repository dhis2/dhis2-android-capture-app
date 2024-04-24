package org.dhis2.form.data.metadata

import org.hisp.dhis.android.core.D2
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
    private lateinit var fileResourceConfiguration: FileResourceConfiguration

    @Before
    fun setUp() {
        fileResourceConfiguration = FileResourceConfiguration(d2)

        whenever(d2.fileResourceModule()).thenReturn(fileResourceModule)
        whenever(fileResourceModule.fileResources()).thenReturn(fileResources)
        whenever(fileResources.uid(anyString())).thenReturn(fileResourceObjectRepository)
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
}

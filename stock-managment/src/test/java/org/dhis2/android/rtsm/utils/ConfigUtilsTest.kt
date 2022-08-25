package org.dhis2.android.rtsm.utils

import junit.framework.TestCase.assertNotNull
import org.dhis2.android.rtsm.exceptions.InitializationException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class ConfigUtilsTest {
    @Mock
    private lateinit var configProps: Properties

    @Before
    fun setup() {
        doReturn("xNamKi10rT")
            .whenever(configProps)
            .getProperty("program")
    }

    @Test(expected = InitializationException::class)
    fun shouldThrowError_ifConfigKeyIsEmpty() {
        ConfigUtils.getConfigValue(configProps, "")
    }

    @Test
    fun shouldReturnConfigValue_ifConfigIsSet() {
        assertNotNull(
            ConfigUtils.getConfigValue(configProps, "program")
        )
    }
}
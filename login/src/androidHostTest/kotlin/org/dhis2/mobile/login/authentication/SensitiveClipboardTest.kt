package org.dhis2.mobile.login.authentication

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.os.Build
import android.os.PersistableBundle
import org.dhis2.mobile.login.authentication.SensitiveClipboard.Companion.CLIP_LABEL
import org.dhis2.mobile.login.authentication.SensitiveClipboard.Companion.EXTRA_IS_SENSITIVE_LEGACY
import org.mockito.MockedConstruction
import org.mockito.MockedStatic
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SensitiveClipboardTest {
    private val clipboard: ClipboardManager = mock()
    private val clipDescription: ClipDescription = mock()
    private val secretClip: ClipData = mock()
    private val emptyClip: ClipData = mock()

    private lateinit var clipDataStatic: MockedStatic<ClipData>
    private lateinit var bundleConstruction: MockedConstruction<PersistableBundle>

    @BeforeTest
    fun setUp() {
        clipDataStatic = mockStatic(ClipData::class.java)
        clipDataStatic
            .`when`<ClipData> { ClipData.newPlainText(CLIP_LABEL, SECRET) }
            .thenReturn(secretClip)
        clipDataStatic.`when`<ClipData> { ClipData.newPlainText("", "") }.thenReturn(emptyClip)
        bundleConstruction = mockConstruction(PersistableBundle::class.java)
        whenever(secretClip.description).thenReturn(clipDescription)
    }

    @AfterTest
    fun tearDown() {
        clipDataStatic.close()
        bundleConstruction.close()
    }

    @Test
    fun `copy flags the clip as sensitive on API 33+`() {
        SensitiveClipboard(clipboard, Build.VERSION_CODES.TIRAMISU).copy(SECRET)

        val extras = bundleConstruction.constructed().single()
        verify(extras).putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        verify(clipDescription).extras = extras
        verify(clipboard).setPrimaryClip(secretClip)
    }

    @Test
    fun `copy flags the clip as sensitive with the literal key below API 33`() {
        SensitiveClipboard(clipboard, Build.VERSION_CODES.S_V2).copy(SECRET)

        val extras = bundleConstruction.constructed().single()
        verify(extras).putBoolean(EXTRA_IS_SENSITIVE_LEGACY, true)
        verify(clipDescription).extras = extras
        verify(clipboard).setPrimaryClip(secretClip)
    }

    @Test
    fun `copy sets no extras on API 23, where ClipDescription has none`() {
        SensitiveClipboard(clipboard, Build.VERSION_CODES.M).copy(SECRET)

        assertTrue(bundleConstruction.constructed().isEmpty())
        verify(clipDescription, never()).extras = any()
        verify(clipboard).setPrimaryClip(secretClip)
    }

    @Test
    fun `clearIfOwned clears the clipboard on API 28+ when it holds the secret`() {
        givenPrimaryClipLabel(CLIP_LABEL)

        SensitiveClipboard(clipboard, Build.VERSION_CODES.P).clearIfOwned()

        verify(clipboard).clearPrimaryClip()
        verify(clipboard, never()).setPrimaryClip(any())
    }

    @Test
    fun `clearIfOwned overwrites with an empty clip below API 28 when it holds the secret`() {
        givenPrimaryClipLabel(CLIP_LABEL)

        SensitiveClipboard(clipboard, Build.VERSION_CODES.O_MR1).clearIfOwned()

        verify(clipboard).setPrimaryClip(emptyClip)
        verify(clipboard, never()).clearPrimaryClip()
    }

    @Test
    fun `clearIfOwned keeps content copied by the user afterwards`() {
        givenPrimaryClipLabel("authenticator code")

        SensitiveClipboard(clipboard, Build.VERSION_CODES.P).clearIfOwned()

        verify(clipboard, never()).clearPrimaryClip()
        verify(clipboard, never()).setPrimaryClip(any())
    }

    @Test
    fun `clearIfOwned does nothing when the clipboard cannot be read or is empty`() {
        whenever(clipboard.primaryClipDescription).thenReturn(null)

        SensitiveClipboard(clipboard, Build.VERSION_CODES.P).clearIfOwned()

        verify(clipboard, never()).clearPrimaryClip()
        verify(clipboard, never()).setPrimaryClip(any())
    }

    private fun givenPrimaryClipLabel(label: String) {
        val description: ClipDescription = mock()
        whenever(description.label).thenReturn(label)
        whenever(clipboard.primaryClipDescription).thenReturn(description)
    }

    private companion object {
        const val SECRET = "JBSWY3DPEHPK3PXP"
    }
}

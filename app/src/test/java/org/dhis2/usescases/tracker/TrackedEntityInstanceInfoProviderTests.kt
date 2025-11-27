package org.dhis2.usescases.tracker

import androidx.compose.ui.graphics.Color
import org.dhis2.commons.date.DateLabelProvider
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.mobile.commons.model.AvatarProviderConfiguration
import org.dhis2.mobile.commons.model.MetadataIconData
import org.dhis2.tracker.data.ProfilePictureProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.ImageCardData
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TrackedEntityInstanceInfoProviderTests {
    private lateinit var teiInfoProvider: TrackedEntityInstanceInfoProvider

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val profilePictureProvider: ProfilePictureProvider = mock()
    private val dateLabelProvider: DateLabelProvider = mock()
    private val metadataIconProvider: MetadataIconProvider =
        mock {
            on { invoke(any()) } doReturn MetadataIconData.defaultIcon()
        }

    @Before
    fun setup() {
        teiInfoProvider =
            TrackedEntityInstanceInfoProvider(
                d2,
                profilePictureProvider,
                dateLabelProvider,
                metadataIconProvider,
            )
    }

    @Test
    fun `Should return a profile picture if it exists`() {
        val tei = mockedTrackedEntityInstance()
        val programUid = "programUid"
        val firstAttributeValue =
            AdditionalInfoItem(
                key = "key",
                value = "Value",
            )

        whenever(profilePictureProvider.invoke(tei, programUid)) doReturn "/path/to/picture"
        whenever(
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet(),
        ) doReturn mockProgram()
        whenever(
            d2
                .iconModule()
                .icons()
                .key(any())
                .blockingExists(),
        ) doReturn false

        val avatar = teiInfoProvider.getAvatar(tei, programUid, firstAttributeValue)

        assert(avatar is AvatarProviderConfiguration.ProfilePic)
        assert((avatar as AvatarProviderConfiguration.ProfilePic).profilePicturePath == "/path/to/picture")
    }

    @Test
    fun `Should return a metadataIcon if it exists and does not have a profile picture`() {
        val tei = mockedTrackedEntityInstance()
        val programUid = "programUid"
        val firstAttributeValue =
            AdditionalInfoItem(
                key = "key",
                value = "Value",
            )

        whenever(profilePictureProvider.invoke(tei, programUid)) doReturn ""
        whenever(
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet(),
        ) doReturn mockProgram()
        whenever(
            d2
                .iconModule()
                .icons()
                .key(any())
                .blockingExists(),
        ) doReturn true
        whenever(metadataIconProvider.invoke(any())) doReturn
            MetadataIconData(
                imageCardData =
                    ImageCardData.IconCardData(
                        uid = "iconUid",
                        label = "iconLabel",
                        iconRes = "ic_icon_resource",
                        iconTint = Color.Unspecified,
                    ),
                color = Color.Unspecified,
            )

        val avatar = teiInfoProvider.getAvatar(tei, programUid, firstAttributeValue)

        assert(avatar is AvatarProviderConfiguration.Metadata)
        assert((avatar as AvatarProviderConfiguration.Metadata).metadataIconData.getIconRes() == "ic_icon_resource")
    }

    @Test
    fun `Should return a default metadataIcon if tei does not have a profile picture and program is null`() {
        val tei = mockedTrackedEntityInstance()
        val programUid = null
        val firstAttributeValue =
            AdditionalInfoItem(
                key = "key",
                value = "Value",
            )

        whenever(profilePictureProvider.invoke(tei, programUid)) doReturn ""
        whenever(
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet(),
        ) doReturn mockProgram()
        whenever(
            d2
                .iconModule()
                .icons()
                .key(any())
                .blockingExists(),
        ) doReturn true

        val avatar = teiInfoProvider.getAvatar(tei, programUid, firstAttributeValue)

        assert(avatar is AvatarProviderConfiguration.Metadata)
        assert((avatar as AvatarProviderConfiguration.Metadata).metadataIconData.getIconRes() == "")
    }

    @Test
    fun `Should return the first character of attribute value if tei does not have an icons and does not have a profile picture`() {
        val tei = mockedTrackedEntityInstance()
        val programUid = "programUid"
        val firstAttributeValue =
            AdditionalInfoItem(
                key = "key",
                value = "First Attribute Value",
            )

        whenever(profilePictureProvider.invoke(tei, programUid)) doReturn ""
        whenever(
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet(),
        ) doReturn mockProgram()
        whenever(
            d2
                .iconModule()
                .icons()
                .key(any())
                .blockingExists(),
        ) doReturn false

        val avatar = teiInfoProvider.getAvatar(tei, programUid, firstAttributeValue)

        assert(avatar is AvatarProviderConfiguration.MainValueLabel)
        assert((avatar as AvatarProviderConfiguration.MainValueLabel).firstMainValue == "F")
    }

    @Test
    fun `Should return empty string of attribute value if tei does not have an icons and does not have a profile picture`() {
        val tei = mockedTrackedEntityInstance()
        val programUid = "programUid"

        whenever(profilePictureProvider.invoke(tei, programUid)) doReturn ""
        whenever(
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet(),
        ) doReturn mockProgram()
        whenever(
            d2
                .iconModule()
                .icons()
                .key(any())
                .blockingExists(),
        ) doReturn false

        val avatar = teiInfoProvider.getAvatar(tei, programUid, null)

        assert(avatar is AvatarProviderConfiguration.MainValueLabel)
        assert((avatar as AvatarProviderConfiguration.MainValueLabel).firstMainValue == "")
    }

    private fun mockedTrackedEntityInstance() =
        TrackedEntityInstance
            .builder()
            .uid("teiUid")
            .trackedEntityType("teTypeUid")
            .build()

    private fun mockProgram(hasStyle: Boolean = false): Program {
        val program = Program.builder().uid("programUid")

        if (hasStyle) {
            program.style(
                ObjectStyle
                    .builder()
                    .icon("icon")
                    .color("blue")
                    .build(),
            )
        }

        return program.build()
    }
}

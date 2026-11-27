package org.dhis2.mobile.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.hisp.dhis.mobile.ui.designsystem.resource.provideDHIS2Icon

/**
 * Icon of a home navigation item, kept as plain data so items stay stable and comparable.
 * It is resolved to a [Painter] only when drawn, through [painter].
 */
sealed interface DrawerIcon {
    /** A Material icon, usually from the extended icons set. */
    data class Vector(
        val image: ImageVector,
    ) : DrawerIcon

    /**
     * An icon shipped as a resource by the DHIS2 design system.
     *
     * @param name the design system drawable name, e.g. `dhis2_doctor_positive`.
     */
    data class DesignSystem(
        val name: String,
    ) : DrawerIcon
}

/** Resolves this icon to a [Painter] for drawing. */
@Composable
fun DrawerIcon.painter(): Painter =
    when (this) {
        is DrawerIcon.Vector -> rememberVectorPainter(image)
        is DrawerIcon.DesignSystem -> provideDHIS2Icon(name)
    }

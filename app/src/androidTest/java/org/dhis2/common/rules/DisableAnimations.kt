package org.dhis2.common.rules

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.IOException

class DisableAnimations : TestRule {

    override fun apply(base: Statement,
                       description: Description): Statement = DisableAnimationsStatement(base)
    inner class DisableAnimationsStatement(private val base: Statement) : Statement() {
        override fun evaluate() {
            disableAnimations()
            try {
                base.evaluate()
            } finally {
                enableAnimations()
            }
        }

        @Throws(IOException::class)
        private fun disableAnimations() {
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                    .executeShellCommand("settings put global transition_animation_scale 0")
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                    .executeShellCommand("settings put global window_animation_scale 0")
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                    .executeShellCommand("settings put global animator_duration_scale 0")
        }

        @Throws(IOException::class)
        private fun enableAnimations() {
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                    .executeShellCommand("settings put global transition_animation_scale 1")
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                    .executeShellCommand("settings put global window_animation_scale 1")
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                    .executeShellCommand("settings put global animator_duration_scale 1")
        }
    }
}
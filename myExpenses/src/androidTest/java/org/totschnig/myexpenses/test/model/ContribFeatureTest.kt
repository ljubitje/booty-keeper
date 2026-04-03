package org.totschnig.myexpenses.test.model

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.totschnig.myexpenses.TestApp
import org.totschnig.myexpenses.model.ContribFeature
import org.totschnig.myexpenses.testutils.MockLicenceHandler

class ContribFeatureTest {

    // Booty: all features are free — recordUsage is a no-op since hasAccessTo always returns true,
    // and trial expiry is irrelevant since hasTrialAccessTo always returns true via hasAccessTo
    @Test
    fun testAllFeaturesAccessible() {
        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        val licenceHandler = application.licenceHandler as MockLicenceHandler
        for (feature in ContribFeature.entries) {
            assertThat(licenceHandler.hasAccessTo(feature)).isTrue()
            assertThat(licenceHandler.hasTrialAccessTo(feature)).isTrue()
        }
    }
}
@file:Suppress("JUnitMalformedDeclaration")

package org.totschnig.myexpenses.util.licence

import com.google.android.vending.licensing.PreferenceObfuscator
import com.google.common.truth.Truth
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.totschnig.myexpenses.MyApplication
import org.totschnig.myexpenses.db2.Repository
import org.totschnig.myexpenses.preference.PrefHandler
import org.totschnig.myexpenses.util.CurrencyFormatter
import org.totschnig.myexpenses.util.crashreporting.CrashHandler

@RunWith(JUnitParamsRunner::class)
class LicenceHandlerTest {
    private lateinit var licenceHandler: LicenceHandler

    @Before
    fun setUp() {
        licenceHandler = LicenceHandler(
            Mockito.mock(MyApplication::class.java),
            Mockito.mock(PreferenceObfuscator::class.java),
            Mockito.mock(CrashHandler::class.java),
            Mockito.mock(PrefHandler::class.java),
            Mockito.mock(Repository::class.java),
            Mockito.mock(CurrencyFormatter::class.java)
        )
    }

    // Booty: all features are free — isEnabledFor always returns true
    @Test
    @Parameters(
        "null, CONTRIB, true",
        "null, EXTENDED, true",
        "null, PROFESSIONAL, true",
        "CONTRIB, CONTRIB, true",
        "CONTRIB, EXTENDED, true",
        "CONTRIB, PROFESSIONAL, true",
        "EXTENDED, CONTRIB, true",
        "EXTENDED, EXTENDED, true",
        "EXTENDED, PROFESSIONAL, true",
        "PROFESSIONAL, CONTRIB, true",
        "PROFESSIONAL, EXTENDED, true",
        "PROFESSIONAL, PROFESSIONAL, true"
    )
    fun isEnabledFor(hasStatus: String, requestedStatus: String, expected: Boolean) {
        licenceHandler.licenceStatus = parse(hasStatus)
        Truth.assertThat(licenceHandler.isEnabledFor(LicenceStatus.valueOf(requestedStatus)))
            .isEqualTo(expected)
    }

    // Booty: no upgrades — everything is already unlocked
    @Test
    @Parameters("null, false", "CONTRIB, false", "EXTENDED, false", "PROFESSIONAL, false")
    fun isUpgradeable(hasStatus: String, expected: Boolean) {
        licenceHandler.licenceStatus = parse(hasStatus)
        Truth.assertThat(licenceHandler.isUpgradeable).isEqualTo(expected)
    }

    @Test
    @Parameters(
        "CONTRIB, null, true",
        "CONTRIB, CONTRIB, true",
        "CONTRIB, EXTENDED, false",
        "CONTRIB, PROFESSIONAL, false",
        "EXTENDED, null, true",
        "EXTENDED, CONTRIB, true",
        "EXTENDED, EXTENDED, true",
        "EXTENDED, PROFESSIONAL, false",
        "PROFESSIONAL, null, true",
        "PROFESSIONAL, CONTRIB, true",
        "PROFESSIONAL, EXTENDED, true",
        "PROFESSIONAL, PROFESSIONAL, true"
    )
    fun greaterOrEqual(self: String, other: String, expected: Boolean) {
        Truth.assertThat(parse(self)!!.greaterOrEqual(parse(other))).isEqualTo(expected)
    }

    private fun parse(licenceStatus: String?) = licenceStatus
        .takeIf { it != "null" }
        ?.let { LicenceStatus.valueOf(it) }
}
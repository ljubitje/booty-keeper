package org.totschnig.myexpenses.testutils

import com.google.android.vending.licensing.PreferenceObfuscator
import org.totschnig.myexpenses.MyApplication
import org.totschnig.myexpenses.db2.Repository
import org.totschnig.myexpenses.di.LicenceModule
import org.totschnig.myexpenses.preference.PrefHandler
import org.totschnig.myexpenses.util.ICurrencyFormatter
import org.totschnig.myexpenses.util.crashreporting.CrashHandler
import org.totschnig.myexpenses.util.licence.LicenceHandler
import java.time.Clock

// Booty: simplified — no Obfuscator/deviceId overrides needed
class MockLicenceModule(private val clock: Clock) : LicenceModule() {
    override fun providesLicenceHandler(
        preferenceObfuscator: PreferenceObfuscator,
        crashHandler: CrashHandler,
        application: MyApplication,
        prefHandler: PrefHandler,
        repository: Repository,
        currencyFormatter: ICurrencyFormatter
    ): LicenceHandler = MockLicenceHandler(application, preferenceObfuscator, crashHandler, prefHandler, repository, currencyFormatter, clock)
}

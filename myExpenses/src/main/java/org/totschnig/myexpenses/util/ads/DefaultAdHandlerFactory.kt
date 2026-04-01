package org.totschnig.myexpenses.util.ads

import android.content.Context
import org.totschnig.myexpenses.preference.PrefHandler
import org.totschnig.myexpenses.util.licence.LicenceHandler
import org.totschnig.myexpenses.util.tracking.Tracker

@Suppress("unused")
open class
DefaultAdHandlerFactory(
    protected val context: Context,
    protected val prefHandler: PrefHandler,
    protected val userCountry: String,
    private val licenceHandler: LicenceHandler,
    protected val tracker: Tracker
) : AdHandlerFactory {
    override val isAdDisabled: Boolean
        get() = true
}

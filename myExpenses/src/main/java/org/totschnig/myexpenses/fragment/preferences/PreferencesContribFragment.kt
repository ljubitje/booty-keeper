package org.totschnig.myexpenses.fragment.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import org.totschnig.myexpenses.activity.PreferenceActivity
import org.totschnig.myexpenses.injector
import org.totschnig.myexpenses.util.licence.LicenceHandler
import javax.inject.Inject


@Keep
class PreferencesContribFragment : Fragment() {

    val preferenceActivity get() = requireActivity() as PreferenceActivity

    @Inject
    lateinit var licenceHandler: LicenceHandler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            // Booty: all features are free — show simple "all unlocked" UI
            setContent {
                licenceHandler.ManageLicence(
                    contribBuyDo = { /* no-op: all features already free */ },
                    validateLicence = {
                        preferenceActivity.validateLicence()
                    },
                    removeLicence = { /* no-op: nothing to remove */ },
                    manageSubscription = { /* no-op: no subscriptions */ }
                )
            }
        }
    }
}
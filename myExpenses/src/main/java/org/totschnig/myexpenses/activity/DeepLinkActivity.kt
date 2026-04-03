package org.totschnig.myexpenses.activity

import android.content.Intent
import android.os.Bundle
import org.totschnig.myexpenses.R

// Booty: simplified — no PayPal callbacks, no licence validation deep links
class DeepLinkActivity : ProtectedFragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            if (Intent.ACTION_VIEW == intent.action) {
                showWebSite()
            }
        }
    }

    override fun onMessageDialogDismissOrCancel() {
        finish()
    }

    private fun showWebSite() {
        dispatchCommand(R.id.WEB_COMMAND, null)
        finish()
    }
}

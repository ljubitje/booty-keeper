package org.totschnig.myexpenses.feature

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.livefront.sealedenum.GenSealedEnum
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.totschnig.myexpenses.MyApplication
import org.totschnig.myexpenses.R
import org.totschnig.myexpenses.activity.BaseActivity
import org.totschnig.myexpenses.preference.PrefHandler
import org.totschnig.myexpenses.preference.PrefKey
import org.totschnig.myexpenses.preference.enumValueOrDefault
import org.totschnig.myexpenses.sync.BackendService
import org.totschnig.myexpenses.sync.GenericAccountService
import org.totschnig.myexpenses.util.Utils
import org.totschnig.myexpenses.util.crashreporting.CrashHandler
import java.util.*

enum class Module(@StringRes val labelResId: Int) {
    WEBUI(R.string.title_webui),
    DRIVE(R.string.title_drive),
    DROPBOX(R.string.title_dropbox),
    WEBDAV(R.string.title_webdav),
    ONEDRIVE(R.string.title_onedrive),
    SQLCRYPT(R.string.title_sqlcrypt),
    FINTS(R.string.title_fints),
    JACKSON(R.string.title_jackson);

    val moduleName: String
        get() = name.lowercase(Locale.ROOT)

    companion object {
        fun from(moduleName: String) = valueOf(moduleName.uppercase())

        fun print(context: Context, moduleName: String) = try {
            context.getString(from(moduleName).labelResId)
        } catch (_: IllegalArgumentException) {
            CrashHandler.report(Throwable("Unknown module: $moduleName"))
            moduleName
        }
    }
}

sealed class Feature(vararg val requiredModules: Module) {

    val labelResId: Int = mainModule.labelResId

    val mainModule
        get() = requiredModules.first()

    open suspend fun canUninstall(
        context: Context,
        prefHandler: PrefHandler,
        datastore: DataStore<Preferences>
    ) = false

    @GenSealedEnum
    companion object {
        fun dependentFeatures(moduleName: String) = Feature.values
            .filter {
                it.requiredModules.contains(Module.from(moduleName))
            }

    }

    sealed class SyncBackend(vararg requiredModules: Module) :
        Feature(*requiredModules) {
        override suspend fun canUninstall(
            context: Context,
            prefHandler: PrefHandler,
            datastore: DataStore<Preferences>
        ) =
            GenericAccountService.getAccountNames(context).none { account ->
                account.startsWith(
                    BackendService.entries.first { it.feature == this }.label
                )
            }
    }


    data object WEBUI : Feature(Module.WEBUI) {
        override suspend fun canUninstall(
            context: Context,
            prefHandler: PrefHandler,
            datastore: DataStore<Preferences>
        ) = datastore.data.map { it[prefHandler.getBooleanPreferencesKey(PrefKey.UI_WEB)] }.first() == false
    }

    data object DRIVE : SyncBackend(Module.DRIVE)
    data object DROPBOX : SyncBackend(Module.DROPBOX, Module.JACKSON)
    data object WEBDAV : SyncBackend(Module.WEBDAV)
    data object ONEDRIVE : SyncBackend(Module.ONEDRIVE, Module.JACKSON)
    data object SQLCRYPT: Feature(Module.SQLCRYPT) {
        override suspend fun canUninstall(
            context: Context,
            prefHandler: PrefHandler,
            datastore: DataStore<Preferences>
        ): Boolean {
            return !prefHandler.encryptDatabase
        }
    }
    data object FINTS: Feature(Module.FINTS)
}


fun getLocaleForUserCountry(context: Context) =
    getLocaleForUserCountry(Utils.getCountryFromTelephonyManager(context))

fun getLocaleForUserCountry(country: String?) =
    getLocaleForUserCountry(country, Locale.getDefault())

fun getLocaleForUserCountry(country: String?, defaultLocale: Locale): Locale {
    val localesForCountry = country?.uppercase(Locale.ROOT)?.let {
        Locale.getAvailableLocales().filter { locale -> it == locale.country }
    }
    return if ((localesForCountry?.size ?: 0) == 0) defaultLocale
    else localesForCountry!!.find { locale -> locale.language == defaultLocale.language }
        ?: localesForCountry[0]
}

abstract class FeatureManager {
    lateinit var application: MyApplication
    var callback: Callback? = null
    private val ocrFeature: OcrFeature?
        get() = application.appComponent.ocrFeature()

    open fun initApplication(application: MyApplication) {
        this.application = application
    }

    open fun initActivity(activity: Activity) {}
    open fun isFeatureInstalled(feature: Feature, context: Context) = true

    open fun requestFeature(feature: Feature, context: Context) {}

    open fun requestLocale(language: String) {
        callback?.onLanguageAvailable(language)
    }

    open fun registerCallback(callback: Callback) {
        this.callback = callback
    }

    open fun unregister() {
        callback = null
    }

    open fun allowsUninstall() = false
    open suspend fun installedModules(
        context: Context,
        prefHandler: PrefHandler,
        datastore: DataStore<Preferences>,
        onlyUninstallable: Boolean = true
    ): Set<String> = emptySet()

    open fun installedLanguages(): Set<String> = emptySet()
    open fun uninstallModules(features: Set<String>) {}
    open fun uninstallLanguages(languages: Set<String>) {}
}

interface Callback {
    fun onLanguageAvailable(language: String) {}
    fun onFeatureAvailable(moduleNames: List<String>) {}
    fun onAsyncStartedFeature(feature: Feature) {}
    fun onAsyncStartedLanguage(displayLanguage: String) {}
    fun onError(throwable: Throwable) {}
}
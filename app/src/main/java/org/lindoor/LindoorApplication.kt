package org.lindoor

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import org.lindoor.customisation.Customisation
import org.lindoor.customisation.Texts
import org.lindoor.linphonecore.CoreContext
import org.lindoor.linphonecore.CorePreferences
import org.lindoor.store.DeviceStore
import org.linphone.core.Factory
import org.linphone.core.LogCollectionState
import org.linphone.mediastream.Log

class LindoorApplication : Application() {

    companion object {
        lateinit var instance: LindoorApplication
            private set
        lateinit var corePreferences: CorePreferences
        lateinit var coreContext: CoreContext

        var someActivityRunning: Boolean = false

        fun ensureCoreExists(context: Context) {
            if (::coreContext.isInitialized && !coreContext.stopped) {
                return
            }

            Factory.instance().setLogCollectionPath(context.filesDir.absolutePath)
            Factory.instance().enableLogCollection(LogCollectionState.Enabled)

            corePreferences = CorePreferences(context)
            corePreferences.copyAssetsFromPackage() // TODO Move in the zip - attention not to overwrite .linphone_rc

            val config = Factory.instance().createConfigWithFactory(
                corePreferences.configPath,
                corePreferences.factoryConfigPath
            )
            corePreferences.config = config

            Factory.instance().setDebugMode(corePreferences.debugLogs, Texts.appName)
            Log.i("[Application] Core context created")
            coreContext = CoreContext(context, config)
            coreContext.start()

            // work around https://bugs.linphone.org/view.php?id=7714 - for demo purpose
            if (corePreferences.config.getBool("app","first_launch", true)) {
                corePreferences.config.setBool("app","first_launch", false)
                coreContext.core.videoPayloadTypes.forEach {
                    if (it.description.toLowerCase().contains("vp8")) {
                        it.enable(false)
                    }
                }
            }

        }


    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Customisation
        Texts
        ensureCoreExists(applicationContext)
        DeviceStore
    }

    fun tablet(): Boolean {
        return resources.getBoolean(R.bool.tablet)
    }

    fun smartPhone(): Boolean {
        return !tablet()
    }

    fun landcape(): Boolean {
        val orientation = resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }

}
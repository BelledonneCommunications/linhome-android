package org.lindoor

import android.app.Application
import android.content.Context
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

        fun ensureCoreExists(context: Context) {
            if (::coreContext.isInitialized && !coreContext.stopped) {
                return
            }

            Factory.instance().setLogCollectionPath(context.filesDir.absolutePath)
            Factory.instance().enableLogCollection(LogCollectionState.Enabled)

            corePreferences = CorePreferences(context)
            corePreferences.copyAssetsFromPackage() // TODO Move in the zip - attention not to overwrite .linphone_rc

            val config = Factory.instance().createConfigWithFactory(corePreferences.configPath, corePreferences.factoryConfigPath)
            corePreferences.config = config

            Factory.instance().setDebugMode(corePreferences.debugLogs, Texts.appName)
            Log.i("[Application] Core context created")
            coreContext = CoreContext(context, config)
            coreContext.start()
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


}
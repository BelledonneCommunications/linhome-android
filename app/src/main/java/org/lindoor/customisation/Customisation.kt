package org.lindoor.customisation

import android.content.Context
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.lindoor.LindoorApplication
import org.lindoor.R
import org.lindoor.utils.zip
import org.linphone.core.Config
import org.linphone.core.Factory
import java.io.File

object Customisation {
    private const val zipbrand = "lindoor.zip"
    private var themeXml = File(LindoorApplication.instance.filesDir, "theme.xml")
    private var textsXml = File(LindoorApplication.instance.filesDir, "texts.xml")
    private var deviceTypesXml = File(LindoorApplication.instance.filesDir, "device_types.xml")
    private var actionsTypesXml = File(LindoorApplication.instance.filesDir, "action_types.xml")
    private var methodTypesXml = File(LindoorApplication.instance.filesDir, "method_types.xml")

    var themeConfig: Config
    var textsConfig: Config
    var deviceTypesConfig: Config
    var actionTypesConfig: Config
    var actionsMethodTypesConfig: Config


    init {
        unzipBrandFromAssets(LindoorApplication.instance)

        themeConfig = Factory.instance().createConfig(null)
        themeConfig.loadFromXmlFile(themeXml.absolutePath)

        textsConfig = Factory.instance().createConfig(null)
        textsConfig.loadFromXmlFile(textsXml.absolutePath)

        deviceTypesConfig = Factory.instance().createConfig(null)
        deviceTypesConfig.loadFromXmlFile(deviceTypesXml.absolutePath)

        actionTypesConfig = Factory.instance().createConfig(null)
        actionTypesConfig.loadFromXmlFile(actionsTypesXml.absolutePath)

        actionsMethodTypesConfig = Factory.instance().createConfig(null)
        actionsMethodTypesConfig.loadFromXmlFile(methodTypesXml.absolutePath)


    }

    private fun unzipBrandFromAssets(context: Context) {
        try {
            val zipInputStream = context.assets.open(zipbrand)
            val zipmd5 = String(Hex.encodeHex(DigestUtils.md5(zipInputStream)))
            if (zipmd5 == context.getSharedPreferences(
                    context.getString(R.string.app_name),
                    Context.MODE_PRIVATE
                ).getString(context.getString(R.string.zip_md5), null)
            ) {
                android.util.Log.i("", "[Customisation] md5 identical - custo has not changed.")
                zipInputStream.close()
                return
            }
            if (zip.unzipInputStream(
                    zipInputStream,
                    LindoorApplication.instance.filesDir.absolutePath
                )
            )
                context.getSharedPreferences(
                    context.getString(R.string.app_name),
                    Context.MODE_PRIVATE
                ).edit().putString(context.getString(R.string.zip_md5), zipmd5).apply()
        } catch (e: Exception) {
            android.util.Log.e("", "[Customisation] Failed unzipping zip from assets : $e")
            e.printStackTrace()
            return
        }
    }

}

/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linhome-android
 * (see https://www.linhome.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.linhome.customisation

import android.content.Context
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.linhome.LinhomeApplication
import org.linhome.R
import org.linhome.utils.zip
import org.linphone.core.Config
import org.linphone.core.Factory
import java.io.File

object Customisation {
    private const val zipbrand = "linhome.zip"
    private var themeXml = File(LinhomeApplication.instance.filesDir, "theme.xml")
    private var textsXml = File(LinhomeApplication.instance.filesDir, "texts.xml")
    private var deviceTypesXml = File(LinhomeApplication.instance.filesDir, "device_types.xml")
    private var actionsTypesXml = File(LinhomeApplication.instance.filesDir, "action_types.xml")
    private var methodTypesXml = File(LinhomeApplication.instance.filesDir, "method_types.xml")

    var themeConfig: Config
    var textsConfig: Config
    var deviceTypesConfig: Config
    var actionTypesConfig: Config
    var actionsMethodTypesConfig: Config


    init {
        unzipBrandFromAssets(LinhomeApplication.instance)

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
                    LinhomeApplication.instance.filesDir.absolutePath
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

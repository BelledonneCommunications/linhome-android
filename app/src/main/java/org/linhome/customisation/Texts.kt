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

import org.linhome.customisation.Customisation.textsConfig
import java.util.*

object Texts {

    val appName = pureGet("appname")

    private fun formatText(
        textKey: String,
        args: Array<String>? = null
    ): String { // Args should be in {0} {1} etc .. in text string
        var text = get(textKey)
        args?.forEachIndexed { index, arg ->
            text = text.replace("{$index}", arg)
        }
        return text
    }

    private fun pureGet(key: String):String {
        val deviceLanguage = Locale.getDefault().language.lowercase(Locale.ROOT)
        return textsConfig.let { config ->
            config.getString(key,deviceLanguage,null)?.also { translation ->
                return translation
            } ?: config.getString(key,"default",null)?.also {default ->
                return default
            } ?: key
        }
    }

    fun get(textKey: String, args: Array<String>? = null): String {
        return formatText(textKey,args)
    }

    fun get(textKey: String, oneArg: String): String {
        return get(textKey, arrayOf(oneArg))
    }

    fun get(textKey: String, arg1: String, arg2:String): String {
        return get(textKey, arrayOf(arg1,arg2))
    }

    fun get(key: String): String {
        return pureGet(key).replace("{appname}",appName).replace("\\n",System.lineSeparator())
    }

}

package org.lindoor.customisation

import org.lindoor.LindoorApplication
import org.lindoor.customisation.Customisation.textsConfig
import org.linphone.mediastream.Log
import java.util.*

object Texts {
    private var textsError: Boolean = false

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

    fun get(key: String): String {
        val deviceLanguage = Locale.getDefault().language.toLowerCase(Locale.ROOT)
        textsConfig.also { config ->
            config.getString(key,deviceLanguage,null)?.also { translation ->
                return translation.replace("{appname}",LindoorApplication.appName)
            } ?: config.getString(key,"default",null)?.also {default ->
                return default.replace("{appname}",LindoorApplication.appName)
            }
        }
        Log.e("[Texts] Failed retrieving text:$key")
        textsError = true
        return key
    }

    fun get(textKey: String, args: Array<String>? = null): String {
        return formatText(textKey,args)
    }

    fun get(textKey: String, oneArg: String): String {
        return get(textKey, arrayOf(oneArg))
    }

}

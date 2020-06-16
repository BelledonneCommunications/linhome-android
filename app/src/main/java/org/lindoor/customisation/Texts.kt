package org.lindoor.customisation

import org.lindoor.customisation.Customisation.textsConfig
import org.lindoor.utils.cdlog
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
        val deviceLanguage = Locale.getDefault().language.toLowerCase(Locale.ROOT)
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

package org.lindoor.linphonecore.extensions

import org.linphone.core.Config

fun Config.getString(section:String, key:String, nonNullDefault:String): String {
    return getString(section, key, nonNullDefault)?:nonNullDefault
}

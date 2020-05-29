package org.lindoor.utils.extensions

import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

fun String.sha256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

fun sixDigitsUUID():String {
    return UUID.randomUUID().toString().replace("-".toRegex(), "").toLowerCase().substring(0, 6)
}
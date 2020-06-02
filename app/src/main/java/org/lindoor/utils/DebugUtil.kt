package org.lindoor.utils

fun stackStrace(point: String) {
    try {
        throw RuntimeException("Forced stack trace for debug invoked at : $point")
    } catch (e: RuntimeException) {
        e.printStackTrace()
    }
}

fun cdlog(message:String) {
    println("cdes>${message}")
}
package org.lindoor.managers

import org.lindoor.LindoorApplication
import org.linphone.core.Call
import java.io.File

object RecordingsManager {
    fun recordingPath(call:Call, earlyMedia:Boolean = false):String {
        val path = File("${LindoorApplication.instance.externalCacheDir}/recordings/${call.callLog.callId}", if (earlyMedia) "earlymedia.mkv" else "call.mkv")
        val toto = path.mkdirs()
        return path.absolutePath
    }
}
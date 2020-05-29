package org.lindoor.managers

import org.lindoor.LindoorApplication
import org.linphone.core.Call
import java.io.File

object RecordingsManager {

    fun recordingPath(call:Call, earlyMedia:Boolean = false):String {
        val path = File("${LindoorApplication.instance.filesDir}/recordings/${call.callLog.callId}", if (earlyMedia) "earlymedia.mkv" else "call.mkv")
        path.mkdirs()
        return path.absolutePath
    }
}
package org.lindoor.managers

import org.lindoor.LindoorApplication
import org.lindoor.entities.Account
import org.lindoor.entities.Action
import org.lindoor.entities.Device
import org.linphone.core.Address
import org.linphone.core.Call
import org.linphone.core.Config
import org.linphone.core.Factory
import org.linphone.mediastream.Log
import java.io.File

object RecordingsManager {

    fun recordingPath(call:Call, earlyMedia:Boolean = false):String {
        val path = File("${LindoorApplication.instance.filesDir}/recordinds/${call.callLog.callId}", if (earlyMedia) "earlymedia.mkv" else "call.mkv")
        path.mkdirs()
        return path.absolutePath
    }
}
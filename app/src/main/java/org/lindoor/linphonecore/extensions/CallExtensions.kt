package org.lindoor.linphonecore.extensions

import org.lindoor.LindoorApplication.Companion.coreContext
import org.linphone.core.Call
import org.linphone.core.CallParams
import org.linphone.core.MediaDirection


fun Call.extendedAcceptEarlyMedia() {
    if (state == Call.State.IncomingReceived) {
        val earlyMediaCallParams: CallParams = coreContext.core.createCallParams(this)
        earlyMediaCallParams.videoDirection = MediaDirection.RecvOnly
        earlyMediaCallParams.audioDirection = MediaDirection.Inactive
        earlyMediaCallParams.recordFile = callLog.historyEvent().mediaFileName
        acceptEarlyMediaWithParams(earlyMediaCallParams)
        startRecording()
    }
}

fun Call.extendedAccept() {
    val inCallParams: CallParams = coreContext.core.createCallParams(this)
    inCallParams.videoDirection = MediaDirection.RecvOnly
    inCallParams.audioDirection = MediaDirection.SendRecv
    inCallParams.recordFile = callLog.historyEvent().mediaFileName
    acceptWithParams(inCallParams)
    startRecording()
}



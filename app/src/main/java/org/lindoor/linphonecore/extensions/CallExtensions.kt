package org.lindoor.linphonecore.extensions

import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.entities.HistoryEvent
import org.lindoor.store.HistoryEventStore
import org.linphone.core.*
import org.linphone.core.tools.Log



fun Call.extendedAcceptEarlyMedia() {
    val earlyMediaCallParams: CallParams = coreContext.core.createCallParams(this)
    earlyMediaCallParams.videoDirection = MediaDirection.RecvOnly
    earlyMediaCallParams.audioDirection = MediaDirection.Inactive
    earlyMediaCallParams.recordFile = callLog.historyEvent().mediaFileName
    acceptEarlyMediaWithParams(earlyMediaCallParams)
    startRecording()
}

fun Call.extendedAccept() {
    val inCallParams: CallParams = coreContext.core.createCallParams(this)
    inCallParams.videoDirection = MediaDirection.RecvOnly
    inCallParams.audioDirection = MediaDirection.SendRecv
    inCallParams.recordFile = callLog.historyEvent().mediaFileName
    acceptWithParams(inCallParams)
    startRecording()
}



package org.lindoor.linphonecore.extensions

import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.entities.HistoryEvent
import org.lindoor.store.HistoryEventStore
import org.linphone.core.*
import org.linphone.core.tools.Log


fun Call.historyEvent() : HistoryEvent {
    return callLog.userData?.let {// Outgoing call pass it through this way
        it as HistoryEvent
    } ?:  HistoryEventStore.findHistoryEventByCallId(callLog.callId) ?: HistoryEvent()
}

fun Call.acceptEarlyMedia() {
    val earlyMediaCallParams: CallParams = coreContext.core.createCallParams(this)
    earlyMediaCallParams.videoDirection = MediaDirection.RecvOnly
    earlyMediaCallParams.audioDirection = MediaDirection.Inactive
    earlyMediaCallParams.recordFile = historyEvent().mediaFileName
    acceptEarlyMediaWithParams(earlyMediaCallParams)
    startRecording()
}

fun Call.accept() {
    val inCallParams: CallParams = coreContext.core.createCallParams(this)
    inCallParams.videoDirection = MediaDirection.RecvOnly
    inCallParams.audioDirection = MediaDirection.SendRecv
    inCallParams.recordFile = historyEvent().mediaFileName
    acceptWithParams(inCallParams)
    startRecording()
}

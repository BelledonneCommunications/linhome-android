package org.lindoor.linphonecore.extensions

import org.lindoor.entities.HistoryEvent
import org.lindoor.store.HistoryEventStore
import org.linphone.core.Call
import org.linphone.core.CallLog


fun CallLog.historyEvent(): HistoryEvent {
    if (userData != null) {
        val historyEvent = userData as HistoryEvent
        if (historyEvent.callId == null && callId != null) {
            historyEvent.callId = callId
            HistoryEventStore.persistHistoryEvent(historyEvent)
            userData = null
        }
        return historyEvent
    }

    HistoryEventStore.findHistoryEventByCallId(callId)?.also {
        return it
    }

    val historyEvent = HistoryEvent()
    callId?.also {
        historyEvent.callId = it
        HistoryEventStore.persistHistoryEvent(historyEvent)
    }
    return historyEvent
}


fun CallLog.isNew(): Boolean {
    return historyEvent().let {
        dir == Call.Dir.Incoming && setOf(
            Call.Status.Missed,
            Call.Status.Declined,
            Call.Status.DeclinedElsewhere
        ).contains(status) && !it.viewedByUser
    }
}

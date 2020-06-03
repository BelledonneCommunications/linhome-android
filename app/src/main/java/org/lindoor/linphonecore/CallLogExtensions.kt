package org.lindoor.linphonecore

import org.lindoor.LindoorApplication
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.entities.HistoryEvent
import org.lindoor.store.HistoryEventStore
import org.linphone.core.*
import org.linphone.core.tools.Log



fun CallLog.historyEvent() : HistoryEvent? {
    return callId?.let{
        HistoryEventStore.findHistoryEventByCallId(it)
    }
}

fun CallLog.isNew() : Boolean {
    return historyEvent()?.let {
        dir == Call.Dir.Incoming && setOf(Call.Status.Missed,Call.Status.Declined,Call.Status.DeclinedElsewhere).contains(status) && !it.viewedByUser
    } ?: false
}
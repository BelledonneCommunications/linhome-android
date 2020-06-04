package org.lindoor.ui.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.lindoor.LindoorApplication
import org.lindoor.linphonecore.callLogsWithNonEmptyCallId
import org.lindoor.store.HistoryEventStore
import org.linphone.core.CallLog
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub

class HistoryViewModel : ViewModel() {
    lateinit var history : MutableLiveData<ArrayList<CallLog>>

    val editing = MutableLiveData(false)
    val selectedForDeletion:MutableLiveData<ArrayList<String>> = MutableLiveData(arrayListOf())

    private val coreListener = object : CoreListenerStub() {
        override fun onCallLogUpdated(lc: Core?, newcl: CallLog?) {
            super.onCallLogUpdated(lc, newcl)
            history.postValue(LindoorApplication.coreContext.core.callLogsWithNonEmptyCallId())
        }
    }

    init {
        LindoorApplication.coreContext.core.addListener(coreListener)
        history = MutableLiveData(LindoorApplication.coreContext.core.callLogsWithNonEmptyCallId())
    }


    override fun onCleared() {
        LindoorApplication.coreContext.core.removeListener(coreListener)
        super.onCleared()
    }

    fun toggleSelectAllForDeletion() {
        if (selectedForDeletion.value!!.size == history.value!!.size) {
            selectedForDeletion.value!!.clear()
        } else {
            selectedForDeletion.value!!.clear()
            history.value!!.forEach {
                selectedForDeletion.value!!.add(it.callId)
            }
        }
        notifyDeleteSelectionListUpdated()
    }

    fun notifyDeleteSelectionListUpdated() {
        selectedForDeletion.value = selectedForDeletion.value
    }

    fun deleteSelection() {
        selectedForDeletion.value!!.forEach { callId ->
            HistoryEventStore.removeHistoryEventByCallId(callId)
            LindoorApplication.coreContext.core.findCallLogFromCallId(callId)?.also { log ->
                LindoorApplication.coreContext.core.removeCallLog(log)
            }
            history.value = LindoorApplication.coreContext.core.callLogsWithNonEmptyCallId()
        }
    }
}


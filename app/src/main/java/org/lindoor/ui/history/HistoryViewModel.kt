package org.lindoor.ui.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.lindoor.LindoorApplication
import org.linphone.core.CallLog
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub

class HistoryViewModel : ViewModel() {
    val history : MutableLiveData<ArrayList<CallLog>> = MutableLiveData(LindoorApplication.coreContext.core.callLogs.toCollection(ArrayList()))

    private val coreListener = object : CoreListenerStub() {
        override fun onCallLogUpdated(lc: Core?, newcl: CallLog?) {
            super.onCallLogUpdated(lc, newcl)
            history.postValue(LindoorApplication.coreContext.core.callLogs.toCollection(ArrayList()))
        }
    }

    init {
        LindoorApplication.coreContext.core.addListener(coreListener)
    }

    override fun onCleared() {
        LindoorApplication.coreContext.core.removeListener(coreListener)
        super.onCleared()
    }
}

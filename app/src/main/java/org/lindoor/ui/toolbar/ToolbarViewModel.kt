package org.lindoor.ui.toolbar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ToolbarViewModel   : ViewModel() {
    var activityInprogress = MutableLiveData(false)
}
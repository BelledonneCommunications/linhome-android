package org.lindoor.ui.toolbar

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ToolbarViewModel   : ViewModel() {
    var activityInprogress = MutableLiveData(false)
    var backButtonVisible =  MutableLiveData(false)
    var burgerButtonVisible =  MutableLiveData(true)
    var leftButtonVisible =  MutableLiveData(false)
    var rightButtonVisible =  MutableLiveData(false)

    fun visibility(param:MutableLiveData<Boolean>) : Int {
        if (param.value!!) return View.VISIBLE
        else return View.GONE
    }
}
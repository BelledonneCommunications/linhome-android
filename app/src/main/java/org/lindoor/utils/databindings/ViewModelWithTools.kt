package org.lindoor.utils.databindings

import androidx.lifecycle.ViewModel
import org.lindoor.LindoorApplication
import org.lindoor.customisation.Texts

open class ViewModelWithTools : ViewModel() {

    val core = LindoorApplication.coreContext.core
    val corePref = LindoorApplication.corePreferences

    fun getText(textKey:String):String {
        return Texts.get(textKey)
    }

}
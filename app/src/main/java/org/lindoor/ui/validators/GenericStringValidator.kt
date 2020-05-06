package org.lindoor.ui.validators

import org.lindoor.customisation.Texts

abstract class GenericStringValidator (errorTextKey : String) {
    val errorText =  Texts.get(errorTextKey)
    abstract fun validity(s:CharSequence) : Pair<Boolean,String?>
}
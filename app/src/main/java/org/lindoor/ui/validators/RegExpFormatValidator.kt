package org.lindoor.ui.validators

import org.lindoor.customisation.Texts

class RegExpFormatValidator(private val reggExp:String, errorTextKey:String) : GenericStringValidator(Texts.get(errorTextKey)) {
    override fun validity(s:CharSequence) : Pair<Boolean,String?> {
        if (!reggExp.toRegex().matches(s))
            return Pair(false,errorText)
        return Pair (true,null)
    }

}
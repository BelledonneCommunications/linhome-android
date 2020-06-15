package org.lindoor.ui.validators

import android.text.TextUtils
import org.lindoor.customisation.Texts
import org.lindoor.ui.widgets.LTextInput

class NonEmptyStringMatcherValidator(private val textInput: LTextInput, errorTextKey: String) :
    GenericStringValidator(errorTextKey) {
    override fun validity(s: CharSequence): Pair<Boolean, String?> {
        if (TextUtils.isEmpty(s))
            return Pair(false, Texts.get("input_invalid_empty_field"))
        else if (!textInput.liveString?.value.equals(s.toString()))
            return Pair(false, errorText)
        return Pair(true, null)
    }

}
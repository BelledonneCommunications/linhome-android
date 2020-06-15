package org.lindoor.ui.validators

import android.text.TextUtils
import org.lindoor.customisation.Texts

class NonEmptyWithRegExpFormatValidator(private val reggExp: String, errorTextKey: String) :
    GenericStringValidator(Texts.get(errorTextKey)) {
    override fun validity(s: CharSequence): Pair<Boolean, String?> {
        if (TextUtils.isEmpty(s))
            return Pair(false, Texts.get("input_invalid_empty_field"))
        if (!reggExp.toRegex().matches(s))
            return Pair(false, errorText)
        return Pair(true, null)
    }
}

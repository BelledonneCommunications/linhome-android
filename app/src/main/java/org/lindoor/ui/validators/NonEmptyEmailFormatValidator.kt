package org.lindoor.ui.validators

import android.text.TextUtils
import org.lindoor.customisation.Texts

class NonEmptyEmailFormatValidator : GenericStringValidator (Texts.get("input_invalid_format_email")) {
    override fun validity(s:CharSequence) : Pair<Boolean,String?> {
        if (TextUtils.isEmpty(s))
            return Pair(false,Texts.get("input_invalid_empty_field"))
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches())
            return Pair(false,errorText)
        return Pair (true,null)
    }
}
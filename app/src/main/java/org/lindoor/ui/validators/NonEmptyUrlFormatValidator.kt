package org.lindoor.ui.validators

import android.text.TextUtils
import org.lindoor.customisation.Texts

class NonEmptyUrlFormatValidator : GenericStringValidator (Texts.get("input_invalid_format_uri")) {
    override fun validity(s:CharSequence) : Pair<Boolean,String?> {
        if (TextUtils.isEmpty(s))
            return Pair(false,Texts.get("input_invalid_empty_field"))
        else  if (!android.util.Patterns.WEB_URL.matcher(s).matches())
            return Pair(false,errorText)
        return Pair (true,null)
    }
}


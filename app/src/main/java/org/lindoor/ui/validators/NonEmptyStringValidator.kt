package org.lindoor.ui.validators

import android.text.TextUtils

class NonEmptyStringValidator : GenericStringValidator("input_invalid_empty_field") {
    override fun validity(s: CharSequence): Pair<Boolean, String?> {
        return if (TextUtils.isEmpty(s))
            Pair(false, errorText)
        else
            Pair(true, null)
    }
}
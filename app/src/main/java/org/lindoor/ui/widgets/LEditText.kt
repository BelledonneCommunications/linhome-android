package org.lindoor.ui.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet


class LEditText : androidx.appcompat.widget.AppCompatEditText {

    var normalBackground: Drawable? = null
    var errorBackground: Drawable? = null

    var normalTextColor:  Int? = null
    var errorTextColor:  Int? = null

    var virgin:Boolean = true

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        virgin = true
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                virgin = !(virgin && !TextUtils.isEmpty(s))
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })

    }

    fun errorMode() {
        background = errorBackground
        errorTextColor?.also { setTextColor(it) }
    }

    fun inputMode() {
        background = normalBackground
        normalTextColor?.also { setTextColor(it) }
    }


}


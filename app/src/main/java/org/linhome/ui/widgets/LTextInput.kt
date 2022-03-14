/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linhome-android
 * (see https://www.linhome.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.linhome.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater.from
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import org.linhome.R
import org.linhome.customisation.Texts
import org.linhome.databinding.WidgetTextInputBinding
import org.linhome.ui.validators.GenericStringValidator


class LTextInput : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    lateinit var title: TextView
    lateinit var text: LEditText
    private lateinit var error: TextView

    lateinit var binding: WidgetTextInputBinding
    var liveString: MutableLiveData<String>? = null
    var liveValidity: MutableLiveData<Boolean>? = null
    var inputType: Int? = null


    var validator: GenericStringValidator? = null
        set(value) {
            field = value
            text.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && !text.virgin) {
                    validate()
                } else {
                    clearError()
                }
            }
        }

    var hint: String? = null
        set(value) {
            binding.text.hint = value?.let { Texts.get(it) }
        }

    private fun init(
        context: Context
    ) {
        binding = DataBindingUtil.inflate(from(context), R.layout.widget_text_input, this, true)
        binding.owner = this

        title = binding.title
        text = binding.text
        error = binding.error
    }


    fun validate() {
        validator?.also { v ->
            text.text?.let { it ->
                val validityResult = v.validity(it)
                if (!validityResult.first) {
                    liveValidity?.value = false
                    validityResult.second?.let { error ->
                        setError(error)
                    }
                } else {
                    liveValidity?.value = true
                    clearError()
                }
            }
        }
    }

    fun setError(message: String) {
        text.errorMode()
        error.text = message
    }

    fun clearError() {
        text.inputMode()
        error.text = null
    }


}
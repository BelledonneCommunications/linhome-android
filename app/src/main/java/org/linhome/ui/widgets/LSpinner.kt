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
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import org.linhome.R
import org.linhome.customisation.Texts
import org.linhome.databinding.WidgetSpinnerBinding
import java.lang.reflect.Method


interface LSpinnerListener {
    fun onItemSelected(position: Int)
}

class LSpinner : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    lateinit var binding: WidgetSpinnerBinding


    var titlekey: String? = null
        set(value) {
            value?.also {
                binding.title.text = Texts.get(it)
                binding.title.visibility = View.VISIBLE
            }
        }

    var titletext: String? = null
        set(value) {
            value?.also {
                binding.title.text = it
                binding.title.visibility = View.VISIBLE
            }
        }

    var items: ArrayList<SpinnerItem>? = null
        set(value) {
            binding.spinner.adapter = value?.let { LSpinnerAdapter(it, binding.spinner, this, null) }
        }


    var missingText: String? = null
        set(value) {
            binding.error.text = value?.let { Texts.get(it) }
        }

    var showMissingText: Boolean? = null
        set(value) {
            binding.error.visibility = if (value!!) View.VISIBLE else View.GONE
        }

    var listener: LSpinnerListener? = null


    var initialIndex: Int? = null
        set(value) {
            if (value != null) {
                binding.spinner.setSelection(value)
                (binding.spinner.adapter as LSpinnerAdapter).selectedIndex = value
            }
        }

    var opened = false


    private fun init(
        context: Context
    ) {
        binding = DataBindingUtil.inflate(from(context), R.layout.widget_spinner, this, true)
        binding.owner = this
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                hideSpinnerDropDown(binding.spinner)
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
            }
        }
    }

    fun onItemSelected(position:Int) {
        listener?.onItemSelected(position)
        hideSpinnerDropDown(binding.spinner)
    }

    fun hideSpinnerDropDown(spinner: Spinner?) { // Work around protected method : onDetachedFromWindow
        try {
            val method: Method = Spinner::class.java.getDeclaredMethod("onDetachedFromWindow")
            method.isAccessible = true
            method.invoke(spinner)
            opened = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
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
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import org.linhome.R
import org.linhome.customisation.Texts
import org.linhome.databinding.WidgetSegmentedControlBinding


class LSegmentedControl : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    lateinit var titleTextView: TextView
    var option1TextKey: String? = null
        set(value) {
            binding.option1.text = Texts.get(value!!)
            field = value
        }

    var option2TextKey: String? = null
        set(value) {
            binding.option2.text = Texts.get(value!!)
            field = value
        }
    var option3TextKey: String? = null
        set(value) {
            binding.option3.text = Texts.get(value!!)
            field = value
        }

    var defaultSelected: Int? = null
        set(value) {
            when (value) {
                0 -> binding.option1.performClick()
                1 -> binding.option2.performClick()
                2 -> binding.option3.performClick()
            }
            field = value
        }

    var liveIndex: MutableLiveData<Int>? = null
    lateinit var binding: WidgetSegmentedControlBinding

    private fun init(
        context: Context
    ) {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.widget_segmented_control,
            this,
            true
        )
        binding.owner = this
        titleTextView = binding.title

        binding.option1.setOnClickListener {
            liveIndex?.value = 0
            on(binding.option1)
            off(binding.option2)
            off(binding.option3)
        }
        binding.option2.setOnClickListener {
            liveIndex?.value = 1
            off(binding.option1)
            on(binding.option2)
            off(binding.option3)
        }
        binding.option3.setOnClickListener {
            liveIndex?.value = 2
            off(binding.option1)
            off(binding.option2)
            on(binding.option3)
        }
    }

    private fun on(option: Button) {
        option.isActivated = true
    }

    private fun off(option: Button) {
        option.isActivated = false
    }

}
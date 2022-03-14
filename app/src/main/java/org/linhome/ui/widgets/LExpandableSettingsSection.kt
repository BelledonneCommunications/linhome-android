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
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import org.linhome.BR
import org.linhome.R
import org.linhome.customisation.Texts
import org.linhome.databinding.SettingsWidgetExpandableBinding


class LExpandableSettingsSection : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private lateinit var binding: SettingsWidgetExpandableBinding

    var entries: ArrayList<ViewDataBinding>? = null
        set(value) {
            binding.items.removeAllViews()
            value?.also { them ->
                for (i in them) {
                    binding.items.addView(i.root)
                    i.setVariable(BR.subsection, true)
                }
                them.last().setVariable(BR.hideseparator, true)
            }
        }

    var title: String? = null
        set(value) {
            value?.also {
                binding.title = Texts.get(it)
            }
        }
    var subtitle: String? = null
        set(value) {
            value?.also {
                binding.subtitle = Texts.get(it)
            }
        }
    var enabled: Boolean? = null
        set(value) {
            binding.enabled = value
        }

    private fun init(
        context: Context
    ) {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.settings_widget_expandable, this, true
        )
        binding.root.setOnClickListener {
            if (binding.items.visibility == View.GONE) {
                binding.items.visibility = View.VISIBLE
                binding.arrow.rotation = 180.0f
            } else {
                binding.items.visibility = View.GONE
                binding.arrow.rotation = .0f
            }
        }
    }

}
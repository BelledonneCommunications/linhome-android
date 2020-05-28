package org.lindoor.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.item_action_info.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.R
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import org.lindoor.databinding.WidgetCallButtonBinding


class LCallButton : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    lateinit var binding: WidgetCallButtonBinding

    var icon: String? = null
        set(value) {
            if (value != null) {
                Theme.setIcon(value,binding.root.icon)
            }
        }
    var tint: String? = null
        set(value) {
            binding.root.icon.imageTintList = value?.let { Theme.buildSingleColorStateList(it) }

        }
    var backgroundeffect: String? = null
        set(value) {
            binding.root.icon.backgroundTintList =
                value?.let { Theme.selectionEffectAsColorStateList(it,android.R.attr.state_activated) }
        }

    var text: String? = null
        set(value) {
            binding.root.text.text = value?.let { Texts.get(it) }

        }

    var onClick: OnClickListener? = null
        set(value) {
            binding.root.icon.setOnClickListener(value)
        }

    private fun init(
        context: Context
    ) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context),R.layout.widget_call_button,this, true)
    }



}
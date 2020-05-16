package org.lindoor.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kotlinx.android.synthetic.main.settings_widget_expandable.view.*
import org.lindoor.BR
import org.lindoor.R
import org.lindoor.customisation.Texts
import org.lindoor.databinding.SettingsWidgetExpandableBinding


class LExpandableSettingsSection : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private lateinit var binding: SettingsWidgetExpandableBinding

    var entries: ArrayList<ViewDataBinding> ? = null
        set(value) {
            items.removeAllViews()
            value?.also { them ->
                for (i in them) {
                    binding.root.items.addView(i.root)
                    i.setVariable(BR.subsection,true)
                }
                them.last().setVariable(BR.hideseparator,true)
            }
        }

    var title: String ? = null
        set(value) {
            value?.also {
                binding.title = Texts.get(it)
            }
        }
    var subtitle: String ? = null
        set(value) {
            value?.also {
                binding.subtitle = Texts.get(it)
            }
        }
    var enabled: Boolean ? = null
        set(value) {
            binding.enabled = value
        }

    private fun init(
        context: Context
    ) {
         binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.settings_widget_expandable,this, true)
        binding.root.setOnClickListener {
            if (binding.root.items.visibility == View.GONE) {
                binding.root.items.visibility = View.VISIBLE
                binding.root.arrow.rotation = 180.0f
            } else {
                binding.root.items.visibility = View.GONE
                binding.root.arrow.rotation = .0f
            }
        }
    }

}
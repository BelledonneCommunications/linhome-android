package org.lindoor.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.widget_segmented_control.view.*
import org.lindoor.R
import org.lindoor.customisation.Texts
import org.lindoor.databinding.WidgetSegmentedControlBinding


class LSegmentedControl : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    lateinit var titleTextView : TextView
    var option1TextKey : String? = null
        set(value) {
            binding.option1.text = Texts.get(value!!)
            field = value
        }

    var option2TextKey : String? = null
        set(value) {
            binding.option2.text = Texts.get(value!!)
            field = value
        }
    var option3TextKey : String? = null
        set(value) {
            binding.option3.text = Texts.get(value!!)
            field = value
        }

    var defaultSelected:Int? = null
        set(value) {
            when (value) {
                0 -> binding.root.option1.performClick()
                1 -> binding.root.option2.performClick()
                2 -> binding.root.option3.performClick()
            }
            field = value
        }

    var liveIndex: MutableLiveData<Int>? = null
    lateinit var binding: WidgetSegmentedControlBinding

    private fun init(
        context: Context
    ) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context),R.layout.widget_segmented_control,this, true)
        binding.owner = this
        titleTextView = binding.root.title

        binding.root.option1.setOnClickListener {
            liveIndex?.value = 0
            on( binding.root.option1)
            off( binding.root.option2)
            off( binding.root.option3)
        }
        binding.root.option2.setOnClickListener {
            liveIndex?.value = 1
            off( binding.root.option1)
            on( binding.root.option2)
            off( binding.root.option3)
        }
        binding.root.option3.setOnClickListener {
            liveIndex?.value = 2
            off( binding.root.option1)
            off( binding.root.option2)
            on( binding.root.option3)
        }
    }

    private fun on(option: Button) {
        option.isActivated = true
    }
    private fun off(option:Button) {
        option.isActivated = false
    }

}
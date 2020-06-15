package org.lindoor.ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.R
import org.lindoor.databinding.WidgetRoundRectButtonBinding


class LRoundRectButton : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    lateinit var buttonText: TextView

    private fun init(
        context: Context
    ) {
        val binding: WidgetRoundRectButtonBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.widget_round_rect_button,
            this,
            true
        )
        setBackgroundColor(Color.TRANSPARENT)
        buttonText = binding.root.text
    }


}
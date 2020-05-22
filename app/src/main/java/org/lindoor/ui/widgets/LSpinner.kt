package org.lindoor.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater.from
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.widget_spinner.view.*
import kotlinx.android.synthetic.main.widget_text_input.view.title
import org.lindoor.R
import org.lindoor.customisation.Texts
import org.lindoor.databinding.WidgetSpinnerBinding
import java.lang.reflect.Method
import java.util.*


interface LSpinnerListener {
    fun onItemSelected(position: Int)
}

class LSpinner : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }
    lateinit var binding: WidgetSpinnerBinding


    var titlekey: String? = null
        set(value) {
            value?.also {
                binding.root.title.text =Texts.get(it)
                binding.root.title.visibility = View.VISIBLE
            }
        }

    var titletext: String? = null
        set(value) {
            value?.also {
                binding.root.title.text = it
                binding.root.title.visibility = View.VISIBLE
            }
        }

    var items: ArrayList<SpinnerItem>? = null
        set(value) {
            binding.root.spinner.adapter = value?.let { LSpinnerAdapter(it,binding.root.spinner) }
        }


    var missingText : String? = null
        set(value) {
            binding.root.error.text = value?.let { Texts.get(it) }
        }

    var showMissingText : Boolean? =  null
            set(value) {
                binding.root.error.visibility = if (value!!) View.VISIBLE else View.GONE
            }

    var listener:LSpinnerListener ? = null


    var initialIndex:Int ? = null
        set(value) {
            if (value != null) {
                binding.root.spinner.setSelection(value)
            }
        }

    private fun init(
        context: Context
    ) {
        binding = DataBindingUtil.inflate(from(context), R.layout.widget_spinner,this, true)
        binding.owner = this
        binding.root.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                hideSpinnerDropDown(binding.root.spinner)
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                listener?.onItemSelected(position)
                hideSpinnerDropDown(binding.root.spinner)
            }
        }
    }

    fun hideSpinnerDropDown(spinner: Spinner?) { // Work around protected method : onDetachedFromWindow
        try {
            val method: Method = Spinner::class.java.getDeclaredMethod("onDetachedFromWindow")
            method.setAccessible(true)
            method.invoke(spinner)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
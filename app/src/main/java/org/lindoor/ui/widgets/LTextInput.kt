package org.lindoor.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater.from
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.widget_text_input.view.*
import org.lindoor.R
import org.lindoor.databinding.WidgetTextInputBinding
import org.lindoor.ui.validators.GenericStringValidator


class LTextInput : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private lateinit var title : TextView
    lateinit var text : LEditText
    private lateinit var error : TextView

    lateinit var binding: WidgetTextInputBinding
    var liveString: MutableLiveData<String>? = null
    var liveValidity: MutableLiveData<Boolean>? = null
    var inputType: Int? = null


    var validator:GenericStringValidator?= null
        set(value) {
            field = value
            text.setOnFocusChangeListener { _ , hasFocus ->
                    if (!hasFocus && !text.virgin) {
                        validate()
                    } else {
                        clearError()
                    }
            }
        }

    private fun init(
        context: Context
    ) {
        binding = DataBindingUtil.inflate(from(context), R.layout.widget_text_input,this, true)
        binding.owner = this

        title = binding.root.title
        text = binding.root.text
        error = binding.root.error

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

    fun setError(message:String) {
        text.errorMode()
        error.text = message
    }

    fun clearError() {
        text.inputMode()
        error.text = null
    }


}
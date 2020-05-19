package org.lindoor.utils.databindings

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.widget_round_rect_button.view.root
import kotlinx.android.synthetic.main.widget_round_rect_button_with_icon.view.*
import kotlinx.android.synthetic.main.widget_text_input.view.*
import org.lindoor.LindoorApplication
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import org.lindoor.ui.settings.SettingListener
import org.lindoor.ui.validators.NonEmptyStringMatcherValidator
import org.lindoor.ui.widgets.*


////////////////////////
// ViewGroup
////////////////////////

@BindingAdapter("backgoundcolor")
fun color(view: ViewGroup, colorKey: String) {
    view.setBackgroundColor(Theme.getColor(colorKey))
}

@BindingAdapter("roundRectInput")
fun roundrectbackground(view: ViewGroup, enabled: Boolean) {
    if (enabled)
        view.background = Theme.roundRectInputBackgroundWithColorKey("color_c")
}


////////////////////////
/// Spinner
///////////////////////

@BindingAdapter("popupBackgoundColor", "popupBackgoundRadius")
fun background(view: Spinner, colorKey: String, radius:Float) {
    view.setPopupBackgroundDrawable(Theme.roundRectGradientDrawable(Theme.getColor(colorKey),radius))
}


////////////////////////
// LTextInput
////////////////////////

@BindingAdapter("passConfirmValidatorOf")// Todo move into class
fun passConfirmValidatorWith(textInput: LTextInput,otherTextInput: LTextInput) {
    textInput.validator = NonEmptyStringMatcherValidator(otherTextInput,"input_password_do_not_match")
}

@BindingAdapter("title") // Todo move into class
fun title(textInput: LTextInput,titleKey: String) {
    textInput.title.text = Texts.get(titleKey)
}
@BindingAdapter("hint") // Todo move into class
fun hint(textInput: LTextInput,hintKey: String) {
    textInput.text.hint = Texts.get(hintKey)
}

///////////////////////
/// TextView
///////////////////////

@BindingAdapter("style")
fun style(textView: TextView, name: String) {
    Theme.apply(name,textView)
}

@BindingAdapter("marquee")
fun marquee(textView: TextView, enabled: Boolean) {
    if (enabled) {
        textView.ellipsize = TextUtils.TruncateAt.MARQUEE
        textView.marqueeRepeatLimit = -0x1
        textView.isSelected = true
    }
}

@BindingAdapter(
    "text",
    "text_args",
    requireAll = false
)
fun text(textView: TextView, textKey: String, textArgs: String?) {
    if (textArgs != null) {
        textView.text = Texts.get(textKey,textArgs.split(",").toTypedArray())
    } else
        textView.text = Texts.get(textKey)
}


///////////////////////
/// LEditText
///////////////////////

@BindingAdapter("style")
fun style(textView: LEditText, name: String) {
    Theme.apply(name,textView)
}

@BindingAdapter("hint")
fun hint(textView: LEditText, textKey: String) {
    textView.hint = Texts.get(textKey)
}

///////////////////////
/// Button
///////////////////////

@BindingAdapter("textstyle")
fun ldstyle(button: Button, name: String) {
    Theme.apply(name,button)
}


@BindingAdapter("selection_effect_text")
fun effect(control: AppCompatButton, effectKey: String) {
    control.setTextColor(Theme.selectionEffectAsColorStateList(effectKey,android.R.attr.state_activated))

}

@BindingAdapter("selection_effect_background")
fun effectbg(control: AppCompatButton, effectKey: String) {
    control.backgroundTintList = Theme.selectionEffectAsColorStateList(effectKey,android.R.attr.state_activated)
}


///////////////////////
/// FloatingActionButton
///////////////////////


@BindingAdapter("icon")
fun icon(button: FloatingActionButton, name: String) {
    Theme.setImage(name,button)
}

@BindingAdapter("background")
fun background(button: FloatingActionButton, name: String) {
    button.backgroundTintList = Theme.selectionEffectAsColorStateList(name,android.R.attr.state_activated)
}

@BindingAdapter("tint")
fun tint(button: FloatingActionButton, name: String) {
    button.imageTintList = Theme.buildSingleColorStateList(name)
}

///////////////////////
///Segmented Control
///////////////////////

@BindingAdapter("title")
fun title(control: LSegmentedControl, name: String) {
    control.titleTextView.text = Texts.get(name)
}

@BindingAdapter("background")
fun background(spinner: LSpinner, name: String) {
    spinner.backgroundTintList = Theme.selectionEffectAsColorStateList(name,android.R.attr.state_activated)
}


///////////////////////
/// ImageView
///////////////////////

@BindingAdapter("src")
fun src(image: ImageView, name: String) {
    Theme.setImage(name,image)
}

@BindingAdapter("selection_effect")
fun selection_effect(image: View, key: String) {
    image.background = Theme.selectionEffectAsStateListDrawable(key)
}

@BindingAdapter("foreground_selection_effect")
fun foreground_selection_effect(image: ImageView, key: String) {
    image.imageTintList =  Theme.selectionEffectAsColorStateList(key,android.R.attr.state_activated)
}

///////////////////////
/// LRoundRectButton
///////////////////////

@BindingAdapter("icon") // Todo move into class
fun icon(b: LRoundRectButtonWithIcon, name: String) {
    Theme.setImage(name, b.icon)
}

@BindingAdapter("text") // Todo move into class
fun text(b: LRoundRectButton, name: String) {
    b.buttonText.text = Texts.get(name)
}

@BindingAdapter("primary") // Todo move into class
fun primary(b: LRoundRectButton, important:Boolean) {
    Theme.roundRectButtonBackgroundStates(if (important) "primary_color" else "secondary_color")?.also {
        b.root.background = it
    }
}

@BindingAdapter("android:enabled") // Todo move into class
fun enabled(b: LRoundRectButton, enabled:Boolean) {
    b.root.isEnabled = enabled
}


@BindingAdapter("text") // Todo move into class
fun text(b: LRoundRectButtonWithIcon, name: String) {
    b.buttonText.text = Texts.get(name)
}


@BindingAdapter("primary") // Todo move into class
fun primary(b: LRoundRectButtonWithIcon, important:Boolean) {
    Theme.roundRectButtonBackgroundStates(if (important) "primary_color" else "secondary_color")?.also {
        b.root.background = it
    }
}

@BindingAdapter("android:enabled") // Todo move into class
fun enabled(b: LRoundRectButtonWithIcon, enabled:Boolean) {
    b.root.isEnabled = enabled
}


////////////////////////
/// ViewGroup
///////////////////////

private fun pxFromDp(dp: Int): Int {
    return (dp * LindoorApplication.instance.resources.displayMetrics.density).toInt()
}

@BindingAdapter("gradientBackground")
fun gradientBackground(view: ViewGroup, themeGradientName: String) {
    view.background = Theme.getGradientColor(themeGradientName)
}


@BindingAdapter("shouldSetPaddingStartOnly", "paddingStartOnlyDp")
fun paddingStartOnly(view: ViewGroup, shouldAdd: Boolean, padding: Int
) {
    if (shouldAdd) {
        view.setPadding(pxFromDp(padding), 0, 0, 0)
    }
}


@BindingAdapter("items")
fun items(viewgroup: ViewGroup, items:ArrayList<ViewDataBinding>) {
    viewgroup.removeAllViews()
    items.forEach {
        viewgroup.addView(it.root)
    }
}

@BindingAdapter("items", "refreshOn")
fun refresh(viewgroup: ViewGroup, items:ArrayList<ViewDataBinding>, refresh:Boolean) {
    if (refresh) {
        viewgroup.removeAllViews()
        items.forEach {
            viewgroup.addView(it.root)
        }
    }
}



/////////////////////
/// Linphone Imports
/////////////////////


@BindingAdapter("selectedIndex", "settingListener")
fun spinnerSetting(spinner: Spinner, selectedIndex: Int, listener: SettingListener) {
    spinner.setSelection(selectedIndex, true)

    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            listener.onListValueChanged(position)
        }
    }
}

@BindingAdapter("onClickToggleSwitch")
fun switchSetting(view: View, switchId: Int) {
    val switch: Switch = view.findViewById(switchId)
    view.setOnClickListener { switch.isChecked = !switch.isChecked }
}


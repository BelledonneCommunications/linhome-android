package org.lindoor.utils

import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.BindingAdapter
import kotlinx.android.synthetic.main.widget_round_rect_button.view.root
import kotlinx.android.synthetic.main.widget_round_rect_button_with_icon.view.*
import kotlinx.android.synthetic.main.widget_text_input.view.*
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import org.lindoor.ui.validators.NonEmptyStringMatcherValidator
import org.lindoor.ui.widgets.*

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

@BindingAdapter("text")
fun text(textView: TextView, textKey: String) {
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
///Segmented Control
///////////////////////

@BindingAdapter("title")
fun title(control: LSegmentedControl, name: String) {
    control.titleTextView.text = Texts.get(name)

}




///////////////////////
/// ImageView
///////////////////////

@BindingAdapter("src")
fun src(image: ImageView, name: String) {
    Theme.setImage(name,image)
}

@BindingAdapter("selection_effect")
fun selection_effect(image: ImageView, key: String) {
    image.background = Theme.selectionEffectAsStateListDrawable(key)
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

@BindingAdapter("gradientBackground")
fun gradientBackground(view: ViewGroup, themeGradientName: String) {
    view.background = Theme.getGradientColor(themeGradientName)
}

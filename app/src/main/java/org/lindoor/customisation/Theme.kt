package org.lindoor.customisation

import android.R
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.*
import android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT
import android.util.StateSet
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.bumptech.glide.Glide
import com.caverock.androidsvg.SVG
import org.lindoor.LindoorApplication
import org.lindoor.customisation.Customisation.themeConfig
import org.lindoor.ui.widgets.LEditText
import org.lindoor.utils.pxFromDp
import org.lindoor.utils.stackStrace
import org.lindoor.utils.svgloader.GlideApp
import org.linphone.mediastream.Log
import java.io.File


object Theme {

    private var typeFaces = hashMapOf<String, Typeface?>()
    private var glidesvg = GlideApp.with(LindoorApplication.instance)
    var glidegeneric = Glide.with(LindoorApplication.instance)

    private var themeError: Boolean = false

    private fun makeGradientDrawable(fromColor: Int, toColor: Int, orientation: String): GradientDrawable {
        val realOrientation = when (orientation) {
            "top_bottom" -> GradientDrawable.Orientation.TOP_BOTTOM
            "bottom_top" -> GradientDrawable.Orientation.BOTTOM_TOP
            "left_right" -> GradientDrawable.Orientation.LEFT_RIGHT
            "right_left" -> GradientDrawable.Orientation.RIGHT_LEFT
            else -> GradientDrawable.Orientation.TOP_BOTTOM
        }
        return GradientDrawable(realOrientation, intArrayOf(fromColor, toColor))
    }

    fun getGradientColor(key: String): GradientDrawable? {
        val entireKey = "gradient-color.$key"
        themeConfig.getString(entireKey,"from",null)?.let { from ->
            themeConfig.getString(entireKey,"to",null)?.let { to ->
                return themeConfig.getString(entireKey, "orientation", null)
                    ?.let { orientation ->
                        makeGradientDrawable(
                            getColor(from),
                            getColor(to),
                            orientation
                        )
                    }
            }
        }
        Log.e("[Theme] Failed retrieving gradient color:$key")
        themeError()
        return null
    }

    fun getColor(key: String): Int {

        if (key == "transparent")
            return Color.TRANSPARENT

        themeConfig.getString("colors", key, null)?.let { color ->
            return Color.parseColor(color)
        }
        Log.e("[Theme] Failed retrieving color:$key")
        themeError()
        return 0
    }

    fun arbitraryValue(key: String, default: String): String {
        var result:String? = themeConfig.getString("arbitrary-values", key, null)
        if (result == null) {
            Log.e("[Theme] Failed retrieving arbitrary value:$key")
            themeError()
            result = default
        }
        return result
    }

    fun arbitraryValue(key: String, default: Boolean): Boolean {
        var result:Boolean? = themeConfig.getBool("arbitrary-values", key, default)
        if (result == null) {
            Log.e("[Theme] Failed retrieving arbitrary value:$key")
            themeError()
            result = default
        }
        return result
    }

    fun setIcon(imageName: String, imageView: ImageView) { // Preferred SVG, fallback PNG or full name.
        val svg = File(LindoorApplication.instance.filesDir, "images/$imageName.svg")
        if (svg.exists())
            glidesvg.load(svg).into(imageView)
        else {
            val png = File(LindoorApplication.instance.filesDir, "images/$imageName.png")
            if (png.exists())
                glidegeneric.load(png).into(imageView)
            else {
                val plain = File(LindoorApplication.instance.filesDir, "images/$imageName")
                if (plain.exists())
                    glidegeneric.load(png).into(imageView)
            }
        }
    }


    private fun getTypeFace(key: String): Typeface { // Try form assets in first place, best place as big in side, if not found from the ZIP.
        val path = "fonts/$key.ttf"
        try {
            val fromAssets: Typeface = Typeface.createFromAsset(
                LindoorApplication.instance.applicationContext.assets, path
            )
            if (fromAssets != Typeface.DEFAULT)
                return fromAssets
        } catch (exception:Exception) {
            Log.i("[Theme] font $key is not on assets, trying from zip. be optimized putting in assets.")
        }
        return Typeface.createFromFile(
            File(
                LindoorApplication.instance.filesDir,
                path
            )
        )
    }



    fun roundRectGradientDrawable(color:Int, radius:Float): GradientDrawable {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(color)
        shape.cornerRadius = pxFromDp(radius)
        return shape
    }

    fun roundRectGradientDrawableWithStroke(color:Int, radius:Float, strokeColor:Int, strokeWidth:Int): GradientDrawable {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(color)
        shape.setStroke(strokeWidth,strokeColor)
        shape.cornerRadius = pxFromDp(radius)
        return shape
    }

    fun circleGradientDrawable(colorKey:String): GradientDrawable {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.OVAL
        shape.setColor(Color.TRANSPARENT)
        shape.setStroke(pxFromDp(5.0f).toInt(), getColor(colorKey))
        return shape
    }


    fun radius(key:String, default:Float = 0.0f): Float {
        return  themeConfig.getFloat("arbitrary-values", key, default)
    }

    fun roundRectInputBackgroundWithColorKeyAndRadius(colorKey:String, radiusKey:String): GradientDrawable {
        return roundRectGradientDrawable(getColor(colorKey), themeConfig.getFloat("arbitrary-values", radiusKey, 0.0f))
    }

    fun roundRectInputBackgroundWithColorKeyAndRadiusAndStroke(colorKey:String, radiusKey:String,strokeColorKey:String, strokeWidthDp: Int): GradientDrawable {
        return roundRectGradientDrawableWithStroke(getColor(colorKey), themeConfig.getFloat("arbitrary-values", radiusKey, 0.0f),
            getColor(strokeColorKey), pxFromDp(strokeWidthDp))
    }

    fun apply(textEditKey: String, textEdit: LEditText) {
        apply(textEditKey, textEdit as EditText, true)
        textEdit.normalBackground = textEdit.background
        textEdit.normalTextColor = textEdit.currentTextColor
        val section = "textedit-style.$textEditKey"
        themeConfig.getString(section, "error-background-color", null)?.let { colorString ->
            val color = getColor(colorString)
            val radius = themeConfig.getFloat("arbitrary-values", "user_input_corner_radius", 0.0f)
            textEdit.errorBackground = roundRectGradientDrawable(color, radius)
        }
        themeConfig.getString(section, "error-text-color", null)?.let { color ->
            textEdit.errorTextColor = getColor(color)
        }
        themeConfig.getString(section, "hint-text-color", null)?.let { color ->
            textEdit.setHintTextColor(getColor(color))
        }
    }


    fun apply(textViewKey: String, textView: TextView, editable: Boolean = false) {
        val section = if (editable) "textedit-style.$textViewKey" else "textview-style.$textViewKey"
        themeConfig.getString(section, "text-color", null)?.let { color ->
            textView.setTextColor(getColor(color))
        }
        themeConfig.getString(section, "background-color", null)?.let { colorString ->
            val color =  getColor(colorString)
            val radius = if (editable) themeConfig.getFloat("arbitrary-values", "user_input_corner_radius", 0.0f) else 0.0f
            textView.background = roundRectGradientDrawable(color,radius)
        }
        textView.isAllCaps = themeConfig.getBool(section, "allcaps", false)
        themeConfig.getString("textview-style.$textViewKey", "size", null)?.let {
            textView.textSize = it.toFloat()
        }
        themeConfig.getString(section, "align", null)?.let {
            when (it) {
                "start" -> {
                    textView.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                }
                "center" -> {
                    textView.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
                    textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                }
                "end" -> {
                    textView.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                }
                else -> {
                    textView.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                }
            }
            themeConfig.getString(section, "font", null)?.let { font ->
                if (!typeFaces.containsKey(font)) {
                    typeFaces[font] = getTypeFace(font)
                }
                typeFaces[font].also { typeFace ->
                    textView.typeface = typeFace
                }
            }
        }
    }

    fun alphAnimate(view: View, targetAlpha: Float) {
        view.animate().apply {
            interpolator = LinearInterpolator()
            duration = 200
            alpha(targetAlpha)
            start()
        }
    }

    private fun roundRectShapeDrawableDualColorState(key:String, radius:Float): GradientDrawable? {
        getSelectionEffectColors(key)?.also {
            val shape = GradientDrawable()
            shape.color = buildColorStateList(it.first,it.second, R.attr.state_pressed)
            shape.cornerRadius = pxFromDp(radius)
            return shape
        }
        return null
    }


    fun roundRectButtonBackgroundStates(key:String): GradientDrawable? {
        return roundRectShapeDrawableDualColorState(key,themeConfig.getFloat("arbitrary-values", "round_rect_button_corner_radius", 0.0f))
    }


    fun buildStateListDrawable(
        drawableIdle: Drawable,
        drawablePressed: Drawable
    ): StateListDrawable {

        val states =
            StateListDrawable()

        states.addState(
            intArrayOf(R.attr.state_pressed),
            drawablePressed
        )
        states.addState(
            intArrayOf(-R.attr.state_enabled),
            drawableIdle
        )
        states.addState(
            intArrayOf(R.attr.state_enabled),
            drawableIdle
        )
        states.addState(StateSet.WILD_CARD, drawableIdle)
        return states
    }

    private fun buildStateListDrawable(
        colorIdle: String,
        colorSelected: String
    ): StateListDrawable {
        val states =
            StateListDrawable()

        val idleColor:Int = getColor(colorIdle)

        states.addState(
            intArrayOf(R.attr.state_pressed),
            ColorDrawable(getColor(colorSelected))
        )
        states.addState(
            intArrayOf(-R.attr.state_enabled),
            ColorDrawable(ColorUtils.setAlphaComponent(idleColor, 127))
        )
        states.addState(
            intArrayOf(R.attr.state_enabled),
            ColorDrawable(idleColor)
        )
        states.addState(StateSet.WILD_CARD, ColorDrawable(getColor(colorIdle)))
        return states
    }

    private fun buildColorStateList(
        colorIdle: String,
        colorSelected: String,
        forState:Int ) : ColorStateList {
        val states = arrayOf(
            intArrayOf(forState),
            intArrayOf(-R.attr.state_enabled),
            intArrayOf())
        val idleColor:Int = getColor(colorIdle)
        val colors = intArrayOf(
            getColor(colorSelected),
            ColorUtils.setAlphaComponent(idleColor, 127),
            idleColor)
        return ColorStateList(states, colors)
    }

    fun buildSingleColorStateList(colorIdle: String): ColorStateList {
        val states = arrayOf(intArrayOf())
        val idleColor:Int = getColor(colorIdle)
        val colors = intArrayOf(idleColor)
        return ColorStateList(states, colors)
    }

    fun getSelectionEffectColors(key:String):Pair<String,String>? {
        themeConfig.getString("selection-effect.$key","default",null)?.let { default ->
            themeConfig.getString("selection-effect.$key","selected",null)?.let { selected ->
                return Pair(default,selected)
            }
        }
        Log.e("[Theme] Failed retrieving selection-effect color:$key")
        themeError()
        return null
    }

    fun  selectionEffectAsStateListDrawable(key:String): StateListDrawable? {
        getSelectionEffectColors(key)?.also {
            return buildStateListDrawable(it.first,it.second)
        }
        return null
    }

    fun selectionEffectAsColorStateList  (key:String,forState:Int): ColorStateList? {
        getSelectionEffectColors(key)?.also {
            return buildColorStateList(it.first,it.second,forState)
        }
        return null
    }

    private fun themeError() {
        themeError = true
        stackStrace("Theme")
    }

    fun svgAsPictureDrawable(imageName:String):PictureDrawable {
        val svgFile = File(LindoorApplication.instance.filesDir, "images/$imageName.svg")
        val svg: SVG = SVG.getFromString(svgFile.readText())
        return PictureDrawable(svg.renderToPicture())
    }

}

package com.rashi.dofirst

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Small helpers so the screens can be built in code without layout files. */
object C {
    const val BG = 0xFF121212.toInt()
    const val CARD = 0xFF1E1E1E.toInt()
    const val TEXT = 0xFFF2F2F2.toInt()
    const val MUTED = 0xFFA0A0A0.toInt()
    const val ACCENT = 0xFFFF6B4A.toInt()
    const val GREEN = 0xFF5CD17E.toInt()
    const val RED = 0xFFB3261E.toInt()
}

fun Context.dp(v: Int) = (v * resources.displayMetrics.density).toInt()

fun Context.label(
    s: String,
    size: Float = 16f,
    color: Int = C.TEXT,
    bold: Boolean = false,
    top: Int = 0,
): TextView = TextView(this).apply {
    text = s
    textSize = size
    setTextColor(color)
    if (bold) typeface = Typeface.DEFAULT_BOLD
    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(top) }
}

fun Context.pill(
    s: String,
    bg: Int = C.ACCENT,
    fg: Int = 0xFF000000.toInt(),
    top: Int = 10,
    onClick: () -> Unit,
): Button = Button(this).apply {
    text = s
    isAllCaps = false
    textSize = 16f
    setTextColor(fg)
    background = GradientDrawable().apply { setColor(bg); cornerRadius = dp(14).toFloat() }
    setPadding(dp(16), dp(14), dp(16), dp(14))
    gravity = Gravity.CENTER
    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(top) }
    setOnClickListener { onClick() }
}

fun Context.rounded(color: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(16).toFloat() }

package com.rashi.dofirst

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast

/** The big full-screen "Do this first" page that covers the distracting app. */
class NagActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        show()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        show()
    }

    private fun show() {
        val tasks = Store.tasks(this)
        if (tasks.isEmpty()) { finish(); return }

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(C.RED)
            setPadding(dp(28), dp(56), dp(28), dp(40))
        }

        col.addView(label("⛔ Not now.", 40f, bold = true))
        col.addView(label(
            if (tasks.size == 1) "You still have 1 task left. Do this first:"
            else "You still have ${tasks.size} tasks left. Do this first:",
            18f, 0xFFFFE3DF.toInt(), top = 12,
        ))

        col.addView(label(tasks[0], 30f, bold = true, top = 20).apply {
            background = rounded(0x33000000)
            setPadding(dp(20), dp(24), dp(20), dp(24))
        })

        if (tasks.size > 1) {
            val rest = tasks.drop(1).take(4).joinToString("\n") { "• $it" }
            col.addView(label("Then:\n$rest", 16f, 0xFFFFE3DF.toInt(), top = 18))
        }

        col.addView(pill("I'll do it now", bg = 0xFFFFFFFF.toInt(), fg = C.RED, top = 32) { goHome() })
        col.addView(pill("✓ I finished it", bg = 0xFF2B0B08.toInt(), fg = 0xFFFFFFFF.toInt()) {
            val left = Store.tasks(this).apply { removeAt(0) }
            Store.setTasks(this, left)
            if (left.isEmpty()) {
                Toast.makeText(this, "All tasks done 🎉 Enjoy!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                show()
            }
        })
        col.addView(pill("Give me 5 minutes", bg = 0x00000000, fg = 0xFFFFE3DF.toInt(), top = 18) {
            Store.setSnoozeUntil(this, System.currentTimeMillis() + 5 * 60_000)
            finish()
        })

        setContentView(ScrollView(this).apply {
            setBackgroundColor(C.RED)
            isFillViewport = true
            addView(col)
        })
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        finish()
    }

    @Deprecated("Back always sends you home instead of back to the distraction")
    override fun onBackPressed() {
        goHome()
    }
}

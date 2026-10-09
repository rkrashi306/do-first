package com.rashi.dofirst

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

/**
 * Watches which app is in front. If it's a distraction and you still have tasks,
 * it buzzes and throws the full-screen "Do this first" page on top.
 * While you stay in a distracting app it re-checks every minute.
 */
class WatchService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var currentPkg: String? = null
    private var lastNag = 0L

    private val tick = object : Runnable {
        override fun run() {
            currentPkg?.let { check(it) }
            handler.postDelayed(this, 60_000)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        handler.removeCallbacks(tick)
        handler.postDelayed(tick, 60_000)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || pkg == "com.android.systemui" || isKeyboard(pkg)) return
        currentPkg = pkg
        check(pkg)
    }

    private fun check(pkg: String) {
        if (!Store.focusOn(this)) return
        if (Store.tasks(this).isEmpty()) return
        val now = System.currentTimeMillis()
        if (now < Store.snoozeUntil(this)) return
        if (!Store.isDistraction(this, pkg)) return
        if (now - lastNag < 3_000) return
        lastNag = now

        buzz()
        startActivity(
            Intent(this, NagActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun isKeyboard(pkg: String): Boolean {
        val ime = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: return false
        return ime.startsWith("$pkg/")
    }

    private fun buzz() {
        val v: Vibrator = if (Build.VERSION.SDK_INT >= 31) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }
        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 150, 400, 150, 800), -1))
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }
}

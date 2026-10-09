package com.rashi.dofirst

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this).apply { setBackgroundColor(C.BG); isFillViewport = true }
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(32), dp(20), dp(48))
        }
        scroll.addView(root)
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        root.removeAllViews()
        root.addView(label("Do First", 32f, bold = true))
        root.addView(label("Open a distracting app while tasks are pending and your phone buzzes and shows what to do first.", 14f, C.MUTED, top = 4))

        // 1. Permission
        section("1. Turn on the watcher")
        if (watcherEnabled()) {
            root.addView(label("✅ Watcher is on", 16f, C.GREEN, bold = true, top = 6))
        } else {
            root.addView(label("Do First needs Accessibility access so it can tell which app is open. It never reads what's on your screen.", 14f, C.MUTED, top = 6))
            root.addView(pill("Turn on watcher") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) })
            root.addView(label("Greyed out or \"restricted setting\"? Go to Settings → Apps → Do First → ⋮ (top right) → Allow restricted settings, then try again.", 13f, C.MUTED, top = 8))
        }

        // 2. Tasks
        section("2. Your tasks")
        root.addView(label("The top task is the one you'll be told to do first.", 13f, C.MUTED, top = 4))
        val input = EditText(this).apply {
            hint = "Add a task, e.g. Finish assignment"
            setHintTextColor(C.MUTED)
            setTextColor(C.TEXT)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            imeOptions = EditorInfo.IME_ACTION_DONE
            background = rounded(C.CARD)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(10) }
        }
        val add = {
            val t = input.text.toString().trim()
            if (t.isNotEmpty()) {
                Store.setTasks(this, Store.tasks(this).apply { add(t) })
                render()
            }
        }
        input.setOnEditorActionListener { _, _, _ -> add(); true }
        root.addView(input)
        root.addView(pill("Add task") { add() })

        val tasks = Store.tasks(this)
        if (tasks.isEmpty()) {
            root.addView(label("No tasks yet — nothing will be blocked.", 14f, C.MUTED, top = 10))
        }
        tasks.forEachIndexed { i, t -> root.addView(taskRow(i, t, tasks)) }

        // 3. Focus switch
        section("3. Focus mode")
        root.addView(Switch(this).apply {
            text = "Nag me when I get distracted"
            setTextColor(C.TEXT)
            textSize = 16f
            isChecked = Store.focusOn(this@MainActivity)
            setOnCheckedChangeListener { _, on -> Store.setFocus(this@MainActivity, on) }
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(8) }
        })
        val snooze = Store.snoozeUntil(this) - System.currentTimeMillis()
        if (snooze > 0) {
            root.addView(label("Snoozed for ${snooze / 60_000 + 1} more min", 13f, C.ACCENT, top = 4))
        }

        // 4. Distracting apps
        section("4. Distracting apps")
        root.addView(CheckBox(this).apply {
            text = "Count every game as a distraction"
            setTextColor(C.TEXT)
            isChecked = Store.blockGames(this@MainActivity)
            setOnCheckedChangeListener { _, on -> Store.setBlockGames(this@MainActivity, on) }
        })
        root.addView(label("Tick any other apps that eat your time:", 13f, C.MUTED, top = 6))
        appList()
    }

    private fun section(title: String) {
        root.addView(label(title, 20f, bold = true, top = 28))
    }

    private fun taskRow(i: Int, t: String, tasks: MutableList<String>): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(if (i == 0) 0xFF3A2420.toInt() else C.CARD)
            setPadding(dp(14), dp(8), dp(8), dp(8))
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(8) }
        }
        row.addView(TextView(this).apply {
            text = "${i + 1}. $t"
            textSize = 16f
            setTextColor(C.TEXT)
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        })
        if (i > 0) row.addView(smallBtn("↑") {
            tasks.add(i - 1, tasks.removeAt(i)); Store.setTasks(this, tasks); render()
        })
        row.addView(smallBtn("✓") {
            tasks.removeAt(i); Store.setTasks(this, tasks); render()
        })
        return row
    }

    private fun smallBtn(s: String, onClick: () -> Unit) = Button(this).apply {
        text = s
        textSize = 18f
        setTextColor(C.TEXT)
        background = null
        minWidth = dp(44)
        layoutParams = LinearLayout.LayoutParams(dp(48), dp(44))
        setOnClickListener { onClick() }
    }

    @Suppress("DEPRECATION")
    private fun appList() {
        val pm = packageManager
        val blocked = Store.blocked(this)
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(launcher, 0)
            .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
            .distinctBy { it.first }
            .filter { it.first != packageName }
            .sortedWith(compareBy({ it.first !in blocked }, { it.second.lowercase() }))

        for ((pkg, name) in apps) {
            val game = Store.isGame(this, pkg)
            root.addView(CheckBox(this).apply {
                text = if (game) "$name  (game)" else name
                setTextColor(C.TEXT)
                isChecked = pkg in blocked
                setOnCheckedChangeListener { _, on ->
                    val s = Store.blocked(this@MainActivity)
                    if (on) s.add(pkg) else s.remove(pkg)
                    Store.setBlocked(this@MainActivity, s)
                }
            })
        }
    }

    private fun watcherEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val me = ComponentName(this, WatchService::class.java)
        return enabled.split(':').any {
            ComponentName.unflattenFromString(it) == me
        }
    }
}

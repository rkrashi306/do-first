package com.rashi.dofirst

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import org.json.JSONArray

/** All saved settings: tasks, which apps are distractions, focus on/off, snooze. */
object Store {
    private fun sp(c: Context) = c.getSharedPreferences("dofirst", Context.MODE_PRIVATE)

    val DEFAULT_BLOCKED = setOf(
        "com.instagram.android",
        "com.instagram.lite",
        "com.reddit.frontpage",
    )

    fun tasks(c: Context): MutableList<String> {
        val a = JSONArray(sp(c).getString("tasks", "[]"))
        return MutableList(a.length()) { a.getString(it) }
    }

    fun setTasks(c: Context, t: List<String>) =
        sp(c).edit().putString("tasks", JSONArray(t).toString()).apply()

    fun blocked(c: Context): MutableSet<String> =
        (sp(c).getStringSet("blocked", DEFAULT_BLOCKED) ?: DEFAULT_BLOCKED).toMutableSet()

    fun setBlocked(c: Context, s: Set<String>) =
        sp(c).edit().putStringSet("blocked", HashSet(s)).apply()

    fun focusOn(c: Context) = sp(c).getBoolean("focus", true)
    fun setFocus(c: Context, on: Boolean) = sp(c).edit().putBoolean("focus", on).apply()

    fun blockGames(c: Context) = sp(c).getBoolean("games", true)
    fun setBlockGames(c: Context, on: Boolean) = sp(c).edit().putBoolean("games", on).apply()

    fun snoozeUntil(c: Context) = sp(c).getLong("snooze", 0L)
    fun setSnoozeUntil(c: Context, t: Long) = sp(c).edit().putLong("snooze", t).apply()

    @Suppress("DEPRECATION")
    fun isGame(c: Context, pkg: String): Boolean = try {
        val ai = c.packageManager.getApplicationInfo(pkg, 0)
        ai.category == ApplicationInfo.CATEGORY_GAME ||
            (ai.flags and ApplicationInfo.FLAG_IS_GAME) != 0
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    fun isDistraction(c: Context, pkg: String): Boolean =
        pkg in blocked(c) || (blockGames(c) && isGame(c, pkg))
}

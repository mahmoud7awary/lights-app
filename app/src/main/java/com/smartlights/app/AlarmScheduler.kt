package com.smartlights.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.json.JSONArray
import java.util.Calendar

/** Schedules real Android alarm-clock alarms (they fire exactly, even in Doze / with the screen off). */
object AlarmScheduler {
    const val SLOTS = 4

    private fun alarmIntent(ctx: Context, slot: Int, snooze: Boolean): PendingIntent {
        val i = Intent(ctx, AlarmReceiver::class.java)
        i.action = "com.smartlights.app.ALARM." + slot + (if (snooze) ".snooze" else "")
        i.putExtra("slot", slot)
        i.putExtra("snooze", snooze)
        val code = (if (snooze) 100 else 0) + slot + 1
        return PendingIntent.getBroadcast(
            ctx, code, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showIntent(ctx: Context): PendingIntent =
        PendingIntent.getActivity(
            ctx, 500, Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    /** Next moment (ms) matching hour:minute on one of the days. days: bit0 = Sunday ... bit6 = Saturday. */
    fun nextTrigger(hour: Int, minute: Int, days: Int, now: Calendar = Calendar.getInstance()): Long {
        for (add in 0..7) {
            val c = now.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, add)
            c.set(Calendar.HOUR_OF_DAY, hour)
            c.set(Calendar.MINUTE, minute)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            val bit = c.get(Calendar.DAY_OF_WEEK) - 1          // Calendar.SUNDAY = 1
            if (((days shr bit) and 1) == 1 && c.timeInMillis > now.timeInMillis) return c.timeInMillis
        }
        return -1L
    }

    private fun setAt(ctx: Context, at: Long, pi: PendingIntent) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        am.setAlarmClock(AlarmManager.AlarmClockInfo(at, showIntent(ctx)), pi)
    }

    fun schedule(ctx: Context, slot: Int, hour: Int, minute: Int, days: Int) {
        val at = nextTrigger(hour, minute, days)
        if (at < 0) cancel(ctx, slot) else setAt(ctx, at, alarmIntent(ctx, slot, false))
    }

    fun cancel(ctx: Context, slot: Int) {
        ctx.getSystemService(AlarmManager::class.java).cancel(alarmIntent(ctx, slot, false))
    }

    /** One-shot alarm (snooze, or the 10-second test). */
    fun scheduleAfter(ctx: Context, slot: Int, delayMs: Long) {
        setAt(ctx, System.currentTimeMillis() + delayMs, alarmIntent(ctx, slot, true))
    }

    /** Makes the phone alarms match the saved timer list (call after any change, reboot or app update). */
    fun rescheduleAll(ctx: Context) {
        val arr = try { JSONArray(Store.alarms(ctx)) } catch (e: Exception) { JSONArray() }
        for (slot in 0 until SLOTS) {
            val o = if (slot < arr.length()) arr.optJSONObject(slot) else null
            if (o != null && o.optBoolean("ring", false) && o.optInt("e", 0) == 1 && o.optInt("d", 0) != 0) {
                schedule(ctx, slot, o.optInt("h", 0), o.optInt("m", 0), o.optInt("d", 0))
            } else {
                cancel(ctx, slot)
            }
        }
    }
}

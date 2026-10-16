package com.smartlights.app

import android.content.Context
import org.json.JSONArray

/** Tiny wrapper around SharedPreferences: controller address, phone-ring flags, alarm list. */
object Store {
    private const val DEFAULT_HOST = "192.168.1.37"
    private const val DEFAULT_RING = "[false,false,false,false]"

    private fun prefs(c: Context) = c.getSharedPreferences("lights", Context.MODE_PRIVATE)

    fun host(c: Context): String = prefs(c).getString("host", DEFAULT_HOST) ?: DEFAULT_HOST
    fun setHost(c: Context, v: String) {
        prefs(c).edit().putString("host", v).apply()
    }

    fun ring(c: Context): String = prefs(c).getString("ring", DEFAULT_RING) ?: DEFAULT_RING
    fun setRing(c: Context, slot: Int, on: Boolean) {
        val arr = try { JSONArray(ring(c)) } catch (e: Exception) { JSONArray() }
        while (arr.length() < AlarmScheduler.SLOTS) arr.put(false)
        if (slot in 0 until AlarmScheduler.SLOTS) arr.put(slot, on)
        prefs(c).edit().putString("ring", arr.toString()).apply()
    }

    /** Last timer list sent by the page: [{ring,e,h,m,d}, ...]. Used to re-arm alarms after a reboot. */
    fun alarms(c: Context): String = prefs(c).getString("alarms", "[]") ?: "[]"
    fun setAlarms(c: Context, json: String) {
        prefs(c).edit().putString("alarms", json).apply()
    }
}

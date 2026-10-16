package com.smartlights.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val slot = intent.getIntExtra("slot", -1)
        val snooze = intent.getBooleanExtra("snooze", false)

        val svc = Intent(context, AlarmService::class.java)
        svc.action = AlarmService.ACTION_START
        svc.putExtra("slot", slot)
        context.startForegroundService(svc)

        // A normal timer alarm: arm the next occurrence (tomorrow or the next chosen day).
        if (!snooze && slot >= 0) AlarmScheduler.rescheduleAll(context)
    }
}

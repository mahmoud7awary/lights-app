package com.smartlights.app

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.text.DateFormat
import java.util.Date

/** The full-screen alarm shown over the lock screen: big time, Snooze and Dismiss. */
class AlarmActivity : Activity() {

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun makeButton(label: String, bg: Int, fg: Int, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = label
        b.isAllCaps = false
        b.textSize = 19f
        b.setTextColor(fg)
        val shape = GradientDrawable()
        shape.cornerRadius = dp(20).toFloat()
        shape.setColor(bg)
        b.background = shape
        b.setOnClickListener { onClick() }
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(64))
        lp.topMargin = dp(14)
        b.layoutParams = lp
        return b
    }

    private fun sendToService(action: String) {
        val i = Intent(this, AlarmService::class.java)
        i.action = action
        startService(i)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(Color.parseColor("#0E1020"))
        root.setPadding(dp(28), dp(28), dp(28), dp(28))

        val title = TextView(this)
        title.text = "Lights alarm"
        title.textSize = 18f
        title.setTextColor(Color.parseColor("#8B90B0"))
        title.gravity = Gravity.CENTER

        val time = TextView(this)
        time.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
        time.textSize = 64f
        time.setTextColor(Color.parseColor("#F4F1EA"))
        time.gravity = Gravity.CENTER
        time.setPadding(0, dp(8), 0, dp(36))

        root.addView(title)
        root.addView(time)
        root.addView(makeButton("Snooze " + AlarmService.SNOOZE_MINUTES + " min", Color.parseColor("#2B2F4F"), Color.parseColor("#F4F1EA")) {
            sendToService(AlarmService.ACTION_SNOOZE)
        })
        root.addView(makeButton("Dismiss", Color.parseColor("#FF9F43"), Color.parseColor("#2A1700")) {
            sendToService(AlarmService.ACTION_DISMISS)
        })
        setContentView(root)

        val filter = IntentFilter(AlarmService.ACTION_STOP_UI)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(stopReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stopReceiver, filter)
        }
    }

    override fun onDestroy() {
        try { unregisterReceiver(stopReceiver) } catch (e: Exception) { }
        super.onDestroy()
    }
}

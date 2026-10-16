package com.smartlights.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator

/** Plays the alarm sound (alarm volume), vibrates and shows the full-screen alarm until dismissed. */
class AlarmService : Service() {

    companion object {
        const val ACTION_START = "com.smartlights.app.START"
        const val ACTION_DISMISS = "com.smartlights.app.DISMISS"
        const val ACTION_SNOOZE = "com.smartlights.app.SNOOZE"
        const val ACTION_STOP_UI = "com.smartlights.app.STOP_UI"
        const val CHANNEL = "alarms"
        const val NOTIF_ID = 42
        const val SNOOZE_MINUTES = 10
        const val MAX_RING_MS = 5 * 60 * 1000L
    }

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private var slot = -1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISMISS -> stopRinging()
            ACTION_SNOOZE -> {
                AlarmScheduler.scheduleAfter(this, slot, SNOOZE_MINUTES * 60_000L)
                stopRinging()
            }
            else -> {
                slot = intent?.getIntExtra("slot", -1) ?: -1
                startRinging()
            }
        }
        return START_NOT_STICKY
    }

    private fun startRinging() {
        stopPlayback()
        createChannel()
        val n = buildNotification()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIF_ID, n)
        }
        playSound()
        vibrate()
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ stopRinging() }, MAX_RING_MS)     // give up after 5 minutes
    }

    private fun createChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        val ch = NotificationChannel(CHANNEL, "Alarms", NotificationManager.IMPORTANCE_HIGH)
        ch.setSound(null, null)                  // the sound is played by this service on the alarm stream
        ch.enableVibration(false)
        ch.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        nm.createNotificationChannel(ch)
    }

    private fun buildNotification(): Notification {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val ui = Intent(this, AlarmActivity::class.java)
        ui.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val fullScreen = PendingIntent.getActivity(this, 1, ui, flags)

        val dismissIntent = Intent(this, AlarmService::class.java)
        dismissIntent.action = ACTION_DISMISS
        val snoozeIntent = Intent(this, AlarmService::class.java)
        snoozeIntent.action = ACTION_SNOOZE
        val dismiss = PendingIntent.getService(this, 2, dismissIntent, flags)
        val snooze = PendingIntent.getService(this, 3, snoozeIntent, flags)

        val icon = Icon.createWithResource(this, R.drawable.ic_bulb)
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_bulb)
            .setContentTitle("Lights alarm")
            .setContentText(if (slot >= 0) "Timer " + (slot + 1) else "Test alarm")
            .setCategory(Notification.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(fullScreen)
            .setFullScreenIntent(fullScreen, true)
            .addAction(Notification.Action.Builder(icon, "Snooze $SNOOZE_MINUTES min", snooze).build())
            .addAction(Notification.Action.Builder(icon, "Dismiss", dismiss).build())
            .build()
    }

    private fun playSound() {
        try {
            var uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            mp.setDataSource(this, uri!!)
            mp.isLooping = true
            mp.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            mp.prepare()
            mp.start()
            player = mp
        } catch (e: Exception) {
            // No sound available: the vibration and the full-screen alarm still work.
        }
    }

    private fun vibrate() {
        val v = getSystemService(Vibrator::class.java)
        vibrator = v
        v?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 800, 600), 0))
    }

    private fun stopPlayback() {
        try { player?.stop() } catch (e: Exception) { }
        try { player?.release() } catch (e: Exception) { }
        player = null
        vibrator?.cancel()
    }

    private fun stopRinging() {
        handler.removeCallbacksAndMessages(null)
        stopPlayback()
        sendBroadcast(Intent(ACTION_STOP_UI).setPackage(packageName))      // closes the alarm screen
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopPlayback()
        super.onDestroy()
    }
}

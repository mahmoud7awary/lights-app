package com.smartlights.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView

/** Hosts the Lights page (bundled in assets/index.html) and exposes the phone-alarm functions to it. */
class MainActivity : Activity() {

    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        web.setBackgroundColor(0xFF0E1020.toInt())

        val s = web.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.allowFileAccess = true
        // The page is loaded from the app (file://) and talks to the controller on the home network.
        @Suppress("DEPRECATION")
        s.allowUniversalAccessFromFileURLs = true

        web.webChromeClient = WebChromeClient()          // lets prompt() (rename a light) work
        web.addJavascriptInterface(Bridge(applicationContext), "Android")
        web.loadUrl("file:///android_asset/index.html")

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        web.onResume()
    }

    override fun onPause() {
        web.onPause()
        super.onPause()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}

/** Functions the page can call as Android.xxx() */
class Bridge(private val ctx: Context) {

    @JavascriptInterface
    fun getHost(): String = Store.host(ctx)

    @JavascriptInterface
    fun setHost(host: String) {
        Store.setHost(ctx, host.trim())
    }

    @JavascriptInterface
    fun getRing(): String = Store.ring(ctx)

    @JavascriptInterface
    fun setRing(slot: Int, on: Boolean) {
        Store.setRing(ctx, slot, on)
    }

    /** json = [{"ring":true,"e":1,"h":7,"m":0,"d":127}, ...] one entry per timer slot. */
    @JavascriptInterface
    fun syncAlarms(json: String) {
        Store.setAlarms(ctx, json)
        AlarmScheduler.rescheduleAll(ctx)
    }

    @JavascriptInterface
    fun testAlarm(seconds: Int) {
        AlarmScheduler.scheduleAfter(ctx, -1, seconds * 1000L)
    }

    @JavascriptInterface
    fun status(): String {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        val notif = nm.areNotificationsEnabled()
        val fsi = if (Build.VERSION.SDK_INT >= 34) nm.canUseFullScreenIntent() else true
        return "{\"notif\":" + notif + ",\"fsi\":" + fsi + "}"
    }

    @JavascriptInterface
    fun openSettings(kind: String) {
        val intent = when (kind) {
            "fullscreen" ->
                if (Build.VERSION.SDK_INT >= 34)
                    Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:" + ctx.packageName))
                else
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
            "notifications" ->
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
            else -> Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            ctx.startActivity(intent)
        } catch (e: Exception) {
            try {
                ctx.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (e2: Exception) { }
        }
    }
}

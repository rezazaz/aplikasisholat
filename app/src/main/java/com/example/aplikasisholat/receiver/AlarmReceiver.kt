package com.example.aplikasisholat.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.aplikasisholat.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Waktu Sholat"

        Toast.makeText(context, "$prayerName telah tiba", Toast.LENGTH_LONG).show()

        // Buat notification channel jika diperlukan (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "azan_channel",
                "Notifikasi Adzan",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk alarm waktu sholat"
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Bangun notifikasi
        val notification = NotificationCompat.Builder(context, "azan_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Waktu Sholat")
            .setContentText("$prayerName telah tiba")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        // Mainkan suara notifikasi
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val player = RingtoneManager.getRingtone(context, ringtoneUri)
            player?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Tampilkan notifikasi
        with(NotificationManagerCompat.from(context)) {
            notify((System.currentTimeMillis() % 10000).toInt(), notification)
        }
    }
}

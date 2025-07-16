package com.example.aplikasisholat

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.aplikasisholat.databinding.ActivityAlarmBinding
import java.util.*

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding

    private val prayerNames = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")
    private val prayerHours = listOf(5, 12, 15, 18, 19)
    private val prayerMinutes = listOf(0, 0, 0, 0, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Pengaturan Alarm Sholat"

        setupAlarmList()
    }

    private fun setupAlarmList() {
        binding.layoutAlarmList.removeAllViews()

        for (i in prayerNames.indices) {
            val prayerName = prayerNames[i]
            val hour = prayerHours[i]
            val minute = prayerMinutes[i]

            val container = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(24, 24, 24, 24)
                background = ContextCompat.getDrawable(this@AlarmActivity, R.drawable.bg_card_alarm)
            }

            val textView = TextView(this).apply {
                text = prayerName
                textSize = 18f
                setTextColor(ContextCompat.getColor(this@AlarmActivity, R.color.black))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val switch = Switch(this).apply {
                isChecked = isAlarmEnabled(prayerName)
                setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                    if (isChecked) {
                        setAlarm(prayerName, hour, minute)
                    } else {
                        cancelAlarm(prayerName)
                    }
                }
            }

            container.addView(textView)
            container.addView(switch)
            binding.layoutAlarmList.addView(container)

            // Tambahkan jarak antar item
            binding.layoutAlarmList.addView(TextView(this).apply {
                height = 16
            })
        }
    }

    private fun setAlarm(prayerName: String, hour: Int, minute: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, com.example.aplikasisholat.receiver.AlarmReceiver::class.java).apply {
            putExtra("PRAYER_NAME", prayerName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            prayerName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        saveAlarmStatus(prayerName, true)
    }

    private fun cancelAlarm(prayerName: String) {
        val intent = Intent(this, com.example.aplikasisholat.receiver.AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            prayerName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        saveAlarmStatus(prayerName, false)
    }

    private fun saveAlarmStatus(prayerName: String, isEnabled: Boolean) {
        val pref = getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        pref.edit().putBoolean(prayerName, isEnabled).apply()
    }

    private fun isAlarmEnabled(prayerName: String): Boolean {
        val pref = getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        return pref.getBoolean(prayerName, false)
    }
}

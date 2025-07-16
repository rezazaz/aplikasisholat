package com.example.aplikasisholat

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.aplikasisholat.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt
import com.example.aplikasisholat.database.DatabaseHelper
import com.example.aplikasisholat.utils.PrayerCalculator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DatabaseHelper
    private val handler = Handler()

    private var userLat = -6.9667  // Default: Semarang
    private var userLng = 110.4167

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        db = DatabaseHelper(this)

        // Buat channel notifikasi untuk azan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "azan_channel",
                "Notifikasi Adzan",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Bottom navigation
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    // Tetap di halaman beranda
                    true
                }
                R.id.nav_kiblat -> {
                    val intent = Intent(this, KiblatActivity::class.java)
                    intent.putExtra("lat", userLat)
                    intent.putExtra("lon", userLng)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out)
                    true
                }
                R.id.nav_alarm -> {
                    val intent = Intent(this, AlarmActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out)
                    true
                }
                else -> false
            }
        }

        // Jalankan fungsi utama
        requestLocationPermission()
        updateCurrentTime()
        showRandomQuote()
    }

    private fun updateCurrentTime() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                val sdf = SimpleDateFormat("HH:mm:ss 'WIB'", Locale.getDefault())
                val currentTime = sdf.format(Date())
                binding.textCurrentPrayer.text = currentTime
                handler.postDelayed(this, 1000)
            }
        }, 0)
    }

    private fun showRandomQuote() {
        val quotes = listOf(
            "“Shalat adalah tiang agama.”",
            "“Sesungguhnya shalat itu mencegah dari perbuatan keji dan mungkar.”",
            "“Jangan tinggalkan shalat, karena itu cahaya hidupmu.”"
        )
        binding.textQuote.text = quotes.random()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_kiblat -> {
                startActivity(Intent(this, KiblatActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        }
    }

    private fun getLocation() {
        val locMan = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        var location: Location? = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location == null) {
            location = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }

        if (location != null) {
            userLat = location.latitude
            userLng = location.longitude
        } else {
            binding.textCity.text = "Semarang (Default)"
        }

        try {
            val geo = Geocoder(this, Locale.getDefault())
            val addresses = geo.getFromLocation(userLat, userLng, 1)
            val cityName = addresses?.firstOrNull()?.locality ?: "Tidak Diketahui"
            binding.textCity.text = cityName
        } catch (e: Exception) {
            binding.textCity.text = "Lokasi Tidak Dikenal"
        }

        binding.textDate.text = getHijriDate()

        val cal = Calendar.getInstance()
        val times = PrayerCalculator.calcPrayerTimes(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            userLng, userLat
        )

        val jadwal = listOf(
            Triple("Subuh", times[0], R.drawable.ic_subuh),
            Triple("Dzuhur", times[2], R.drawable.ic_dzuhur),
            Triple("Ashar", times[3], R.drawable.ic_ashar),
            Triple("Maghrib", times[4], R.drawable.ic_maghrib),
            Triple("Isya", times[5], R.drawable.ic_isya)
        )

        binding.layoutWaktuSholat.removeAllViews()
        jadwal.forEach { (name, time, icon) ->
            addPrayerCard(name, formatTime(time), icon)
        }
    }

    private fun addPrayerCard(title: String, time: String, iconRes: Int) {
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0)
            }
            radius = 20f
            cardElevation = 8f
            setCardBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.white))
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 24, 24, 24)
        }

        val icon = ImageView(this).apply {
            setImageResource(iconRes)
            layoutParams = LinearLayout.LayoutParams(64, 64).apply {
                marginEnd = 24
            }
        }

        val text = TextView(this).apply {
            text = "$title\n$time"
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.black))
        }

        layout.addView(icon)
        layout.addView(text)
        card.addView(layout)
        binding.layoutWaktuSholat.addView(card)
    }

    private fun formatTime(time: Double): String {
        val hour = floor(time).toInt()
        val minute = ((time - hour) * 60).roundToInt()
        return String.format("%02d:%02d", hour, minute)
    }

    private fun getHijriDate(): String {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id"))
        val tanggalMasehi = sdf.format(calendar.time)

        val hijriTanggal = calendar.get(Calendar.DAY_OF_MONTH)
        val hijriBulan = calendar.get(Calendar.MONTH) + 1
        val hijriTahun = calendar.get(Calendar.YEAR) - 579

        val hijriBulanNama = listOf(
            "Muharram", "Safar", "Rabiul Awal", "Rabiul Akhir",
            "Jumadil Awal", "Jumadil Akhir", "Rajab", "Sya'ban",
            "Ramadhan", "Syawal", "Dzulkaidah", "Dzulhijjah"
        ).getOrElse((hijriBulan - 1) % 12) { "Muharram" }

        return "$tanggalMasehi / $hijriTanggal $hijriBulanNama $hijriTahun H"
    }
}

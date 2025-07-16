package com.example.aplikasisholat

import android.hardware.*
import android.os.Bundle
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlin.math.*

class KiblatActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var compassView: ImageView
    private lateinit var textDirection: TextView

    // Koordinat Ka'bah di Makkah
    private val kaabaLat = 21.422487
    private val kaabaLng = 39.826206

    // Lokasi pengguna
    private var userLat = 0.0
    private var userLng = 0.0
    private var qiblaDirection = 0.0

    private var currentAzimuth = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kiblat)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarKiblat)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish() // kembali ke halaman utama
        }

        compassView = findViewById(R.id.compassView)
        textDirection = findViewById(R.id.textDirection)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Ambil lokasi dari Intent
        userLat = intent.getDoubleExtra("lat", 0.0)
        userLng = intent.getDoubleExtra("lon", 0.0)

        if (userLat == 0.0 && userLng == 0.0) {
            textDirection.text = "Arah Kiblat: Lokasi tidak tersedia"
        } else {
            qiblaDirection = calculateQiblaDirection(userLat, userLng)
            textDirection.text = "Arah Kiblat: %.2f°".format(qiblaDirection)
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val azimuth = event?.values?.get(0) ?: return
        val angleToQibla = (qiblaDirection - azimuth + 360) % 360

        val rotate = RotateAnimation(
            currentAzimuth,
            -angleToQibla.toFloat(),
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 500
            fillAfter = true
        }

        compassView.startAnimation(rotate)
        currentAzimuth = -angleToQibla.toFloat()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Tidak digunakan
    }

    // Fungsi menghitung arah Kiblat dari posisi pengguna
    private fun calculateQiblaDirection(lat: Double, lng: Double): Double {
        val phiUser = Math.toRadians(lat)
        val lambdaUser = Math.toRadians(lng)
        val phiKaaba = Math.toRadians(kaabaLat)
        val lambdaKaaba = Math.toRadians(kaabaLng)

        val deltaLambda = lambdaKaaba - lambdaUser
        val y = sin(deltaLambda)
        val x = cos(phiUser) * tan(phiKaaba) - sin(phiUser) * cos(deltaLambda)
        val theta = atan2(y, x)

        return (Math.toDegrees(theta) + 360) % 360
    }
}

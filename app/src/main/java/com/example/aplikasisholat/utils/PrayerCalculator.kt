package com.example.aplikasisholat.utils

import kotlin.math.*

object PrayerCalculator {

    private fun degToRad(deg: Double): Double = deg * Math.PI / 180.0
    private fun radToDeg(rad: Double): Double = rad * 180.0 / Math.PI

    private fun safeAcos(x: Double): Double = acos(x.coerceIn(-1.0, 1.0))

    private fun normalizeAngle(angle: Double): Double {
        val a = angle % 360.0
        return if (a < 0) a + 360.0 else a
    }

    /**
     * Menghitung waktu sholat dalam satu hari berdasarkan koordinat dan tanggal.
     * @return Array: [Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha] dalam format jam desimal (misal: 5.75 = 05:45).
     */
    fun calcPrayerTimes(
        year: Int,
        month: Int,
        day: Int,
        longitude: Double,
        latitude: Double,
        timeZone: Int = 7,
        fajrTwilight: Double = -18.0,
        ishaTwilight: Double = -18.0
    ): DoubleArray {

        // Hari Julian
        val D = (367 * year) - ((year + (month + 9) / 12) * 7 / 4) + ((275 * month) / 9) + day - 730531.5

        val L = normalizeAngle(280.461 + 0.9856474 * D) // Mean longitude
        val M = normalizeAngle(357.528 + 0.9856003 * D) // Mean anomaly

        val Lambda = normalizeAngle(
            L + 1.915 * sin(degToRad(M)) + 0.02 * sin(degToRad(2 * M))
        ) // Ecliptic longitude

        val Obliquity = 23.439 - 0.0000004 * D // Obliquity of the ecliptic
        var Alpha = radToDeg(atan(cos(degToRad(Obliquity)) * tan(degToRad(Lambda))))
        Alpha = normalizeAngle(Alpha + 90 * (floor(Lambda / 90) - floor(Alpha / 90)))

        val ST = normalizeAngle(100.46 + 0.985647352 * D)
        val Dec = radToDeg(asin(sin(degToRad(Obliquity)) * sin(degToRad(Lambda))))

        val DurinalArc = radToDeg(
            acos(
                (sin(degToRad(-0.8333)) - sin(degToRad(Dec)) * sin(degToRad(latitude))) /
                        (cos(degToRad(Dec)) * cos(degToRad(latitude)))
            )
        )

        val Noon = normalizeAngle(Alpha - ST)
        val UTNoon = Noon - longitude
        val zuhrTime = UTNoon / 15.0 + timeZone

        // Asr
        val angle = radToDeg(atan(1 / tan(abs(degToRad(latitude - Dec)))))
        val AsrArc = radToDeg(
            safeAcos(
                (sin(degToRad(90 - angle)) - sin(degToRad(Dec)) * sin(degToRad(latitude))) /
                        (cos(degToRad(Dec)) * cos(degToRad(latitude)))
            )
        )
        val asrTime = zuhrTime + (AsrArc / 15.0)

        // Matahari terbit dan terbenam
        val sunriseTime = zuhrTime - (DurinalArc / 15.0)
        val maghribTime = zuhrTime + (DurinalArc / 15.0)

        // Fajr
        val fajrArc = radToDeg(
            acos(
                (sin(degToRad(fajrTwilight)) - sin(degToRad(Dec)) * sin(degToRad(latitude))) /
                        (cos(degToRad(Dec)) * cos(degToRad(latitude)))
            )
        )
        val fajrTime = zuhrTime - (fajrArc / 15.0)

        // Isha
        val ishaArc = radToDeg(
            acos(
                (sin(degToRad(ishaTwilight)) - sin(degToRad(Dec)) * sin(degToRad(latitude))) /
                        (cos(degToRad(Dec)) * cos(degToRad(latitude)))
            )
        )
        val ishaTime = zuhrTime + (ishaArc / 15.0)

        return doubleArrayOf(fajrTime, sunriseTime, zuhrTime, asrTime, maghribTime, ishaTime)
    }
}

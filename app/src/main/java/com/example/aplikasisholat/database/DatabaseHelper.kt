package com.example.aplikasisholat.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "sholat.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "sholat"
        private const val COLUMN_DATE = "tanggal"
        private val PRAYER_COLUMNS = listOf("subuh", "dzuhur", "ashar", "maghrib", "isya")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COLUMN_DATE TEXT PRIMARY KEY,
                subuh TEXT,
                dzuhur TEXT,
                ashar TEXT,
                maghrib TEXT,
                isya TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Jika suatu saat butuh upgrade tabel
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertOrUpdate(
        tanggal: String,
        subuh: String,
        dzuhur: String,
        ashar: String,
        maghrib: String,
        isya: String
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, tanggal)
            put("subuh", subuh)
            put("dzuhur", dzuhur)
            put("ashar", ashar)
            put("maghrib", maghrib)
            put("isya", isya)
        }
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun getToday(tanggal: String): Map<String, String>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_DATE = ?", arrayOf(tanggal))

        return cursor.use {
            if (it.moveToFirst()) {
                val result = PRAYER_COLUMNS.associateWith { column ->
                    cursor.getString(cursor.getColumnIndexOrThrow(column))
                }
                result
            } else {
                null
            }
        }
    }
}

package com.example.projet_kotlin_randazzo_benjamin

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Movie::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDAO

    companion object {
        fun getInstance(applicationContext: Context): AppDatabase {
            return Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "benjamin_base.db"
            ).build()
        }
    }
}
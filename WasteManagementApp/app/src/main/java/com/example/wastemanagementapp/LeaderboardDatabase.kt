package com.example.wastemanagementapp

import androidx.activity.ComponentActivity
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LeaderboardEntry::class], version=1)
abstract class LeaderboardDatabase :RoomDatabase(){
    abstract fun leaderboardDao(): LeaderboardDao

    companion object{
        @Volatile
        private var INSTANCE: LeaderboardDatabase? = null

        fun getDatabase(context: ComponentActivity): LeaderboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LeaderboardDatabase::class.java,
                    "leaderboard_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

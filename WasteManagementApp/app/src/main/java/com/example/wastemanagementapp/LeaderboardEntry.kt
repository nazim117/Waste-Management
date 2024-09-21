package com.example.wastemanagementapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard")
data class LeaderboardEntry(
    @PrimaryKey val userId: String,
    @ColumnInfo(name="score") val score: Int
)

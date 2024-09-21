package com.example.wastemanagementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import java.util.UUID

class MainActivity : ComponentActivity() {
private lateinit var database: LeaderboardDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = LeaderboardDatabase.getDatabase(this)
        var userId = getUserId()

        setContent {
            ScoreSubmissionScreen(
                userId = userId,
                database = database
            )
        }

    }

    private fun getUserId(): String {
        val prefs = getPreferences(MODE_PRIVATE)
        var userId = prefs.getString("userId", null)
        if(userId == null){
            userId = UUID.randomUUID().toString()
            prefs.edit().putString("userId", userId).apply()
        }
        return userId
    }
}
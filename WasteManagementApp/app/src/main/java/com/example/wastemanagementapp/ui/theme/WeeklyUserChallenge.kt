package com.example.wastemanagementapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WeeklyUserChallengeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9))
            .padding(16.dp)
    ) {
        Text(
            text = "Weekly Waste Challenge",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            Icon(Icons.Default.Star, contentDescription = "Star", tint = Color.Gray)
            Icon(Icons.Default.Star, contentDescription = "Star", tint = Color.Gray)
            Icon(Icons.Default.Star, contentDescription = "Star", tint = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ){
            Text(text = "Scan bottle")
        }
    }
}
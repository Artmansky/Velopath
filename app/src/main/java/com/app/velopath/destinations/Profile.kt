package com.app.velopath.destinations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.velopath.ui.TopBar
import com.google.firebase.auth.FirebaseUser
import kotlinx.serialization.Serializable

@Serializable
object Profile

@Composable
fun PrintProfile(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    userData: FirebaseUser?
) {
    Scaffold(
        topBar = {
            TopBar(
                title = title,
                onDrawerClick = onClick
            )
        }
    ) { contentPadding ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(8),
        ) {
            Text(
                text = "Hello Profile",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Hello Profile",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
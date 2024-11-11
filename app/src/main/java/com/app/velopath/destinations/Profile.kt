package com.app.velopath.destinations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.app.velopath.TopBar
import com.google.firebase.auth.FirebaseUser
import kotlinx.serialization.Serializable

@Serializable
object Profile

@Composable
fun PrintProfile(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    onSignOutClick: () -> Unit,
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { onSignOutClick() }
            ) {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
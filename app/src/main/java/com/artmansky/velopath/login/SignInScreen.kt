package com.artmansky.velopath.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.velopath.R
import kotlinx.serialization.Serializable

@Serializable
object SignInScreen

@Composable
fun PrintSignInScreen(
    state: SignInState,
    onSignInClick: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.velopath_icon),
                    contentDescription = "Velopath Icon",
                    modifier = Modifier.size(180.dp)
                )
            }

            Text(
                text = "Welcome to:",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Velopath",
                style = MaterialTheme.typography.displayLarge
            )

            Text(
                text = "Please sign in using",
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = onSignInClick,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "Google",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

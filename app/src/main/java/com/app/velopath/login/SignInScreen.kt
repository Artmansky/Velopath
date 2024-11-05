package com.app.velopath.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.velopath.R
import kotlinx.serialization.Serializable

@Serializable
object SignInScreen

@Composable
fun PrintSignInScreen(
    onSignInClick: () -> Unit
) {
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
                text = "Please continue with",
                style = MaterialTheme.typography.bodyLarge
            )

            GoogleSignInButton(onSignInClick)
        }
    }
}


@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val backgroundColor = if (isDarkTheme) Color.White else Color.Black
    val contentColor = if (isDarkTheme) Color.Black else Color.White

    Button(
        onClick = onSignInClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Image(
            painter = painterResource(id = R.drawable.google_logo_svg),
            contentDescription = "Google Logo",
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text = "Sign in with Google", modifier = Modifier.padding(6.dp))
    }
}
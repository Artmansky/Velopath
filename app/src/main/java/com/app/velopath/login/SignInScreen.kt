package com.app.velopath.login

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import com.app.velopath.R
import kotlinx.serialization.Serializable

@Serializable
object SignInScreen

@Composable
fun PrintSignInScreen(
    onSignInClick: () -> Unit,
    loginStatus: MutableState<Boolean>,
    darkTheme: Boolean,
    context: Context
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
                text = getString(context, R.string.welcome_to),
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = getString(context, R.string.app_name),
                style = MaterialTheme.typography.displayLarge
            )

            Text(
                text = getString(context, R.string.please_continue),
                style = MaterialTheme.typography.bodyLarge
            )

            GoogleSignInButton(onSignInClick, loginStatus, darkTheme, context)
        }
    }
}


@Composable
fun GoogleSignInButton(
    onSignInClick: () -> Unit,
    loginStatus: MutableState<Boolean>,
    isDarkTheme: Boolean,
    context: Context
) {

    val backgroundColor = if (isDarkTheme) Color.White else Color.Black
    val contentColor = if (isDarkTheme) Color.Black else Color.White

    if (loginStatus.value) {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 6.dp
        )
    } else {
        Button(
            onClick = {
                loginStatus.value = true
                onSignInClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = contentColor
            ),
            enabled = !loginStatus.value
        ) {
            Image(
                painter = painterResource(id = R.drawable.google_logo_svg),
                contentDescription = "Google Logo",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = getString(context, R.string.sign_google), modifier = Modifier.padding(6.dp))
        }
    }
}
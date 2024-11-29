package com.app.velopath.destinations

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.app.velopath.destinations.routes.AnimatedExpandableList
import com.app.velopath.destinations.routes.routeItems
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
    userData: FirebaseUser?,
    isDarkMode: Boolean,
    context: Context
) {
    Scaffold(
        topBar = {
            TopBar(
                title = title,
                onDrawerClick = onClick,
            )
        }
    ) { contentPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = userData?.photoUrl,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Black, CircleShape),
                        contentScale = ContentScale.Crop,
                        error = rememberVectorPainter(image = Icons.Default.AccountCircle),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = userData?.displayName ?: "Nickname",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total added", style = MaterialTheme.typography.bodyMedium)
                            Text("Value 1", style = MaterialTheme.typography.bodyLarge)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total liked", style = MaterialTheme.typography.bodyMedium)
                            Text("Value 2", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total likes", style = MaterialTheme.typography.bodyMedium)
                            Text("Value 3", style = MaterialTheme.typography.bodyLarge)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total routes", style = MaterialTheme.typography.bodyMedium)
                            Text("Value 4", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Your added routes:",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            AnimatedExpandableList(routeItems, isDarkMode, context)
        }
    }
}
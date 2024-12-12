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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import coil3.compose.AsyncImage
import com.app.velopath.R
import com.app.velopath.database.FirebaseManagement
import com.app.velopath.database.RouteItem
import com.app.velopath.destinations.routes.AnimatedExpandableList
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
    database: FirebaseManagement,
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
                        text = userData?.displayName ?: getString(context, R.string.nickname),
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
                            Text(
                                getString(context, R.string.total_added),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text("Value 1", style = MaterialTheme.typography.bodyLarge)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                getString(context, R.string.total_liked),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text("Value 2", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                getString(context, R.string.total_likes),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text("Value 3", style = MaterialTheme.typography.bodyLarge)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                getString(context, R.string.total_routes),
                                style = MaterialTheme.typography.bodyMedium
                            )
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
                    text = getString(context, R.string.your_added),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            ProfileListRoutes(isDarkMode, userData, database, context)
        }
    }
}

@Composable
fun ProfileListRoutes(
    isDarkMode: Boolean,
    userData: FirebaseUser?,
    database: FirebaseManagement,
    context: Context
) {
    val fetchedItems = remember { mutableStateOf<List<RouteItem>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        database.fetchAuthorRoutes(
            author = userData?.uid,
            onResult = { routes ->
                fetchedItems.value = routes
                isLoading.value = false
            },
            onFail = {
                isLoading.value = false
            }
        )

    }

    if (isLoading.value) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    } else {
        AnimatedExpandableList(true, fetchedItems.value, isDarkMode, context)
    }
}
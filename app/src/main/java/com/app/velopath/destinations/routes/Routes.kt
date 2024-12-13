package com.app.velopath.destinations.routes

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import com.app.velopath.R
import com.app.velopath.database.FirebaseManagement
import com.app.velopath.database.RouteItem
import com.app.velopath.handlers.isNetworkAvailable
import com.app.velopath.ui.TopBar
import kotlinx.serialization.Serializable

@Serializable
object Routes

@Composable
fun PrintRoutes(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean,
    title: String,
    database: FirebaseManagement,
    onClick: () -> Unit,
    context: Context
) {
    Scaffold(
        topBar = {
            TopBar(
                title = title,
                onDrawerClick = onClick
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            ProfileListRoutes(isDarkMode, database, context)
        }
    }
}

@Composable
fun ProfileListRoutes(
    isDarkMode: Boolean,
    database: FirebaseManagement,
    context: Context
) {
    val fetchedItems = remember { mutableStateOf<List<RouteItem>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (isNetworkAvailable(context)) {
            database.fetchLikedRoutes(
                onResult = { routes ->
                    fetchedItems.value = routes
                    isLoading.value = false
                },
                onFail = {}
            )
        } else {
            isLoading.value = false
            Toast.makeText(
                context,
                getString(context, R.string.error_fetching),
                Toast.LENGTH_SHORT
            ).show()
        }
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
        AnimatedExpandableList(
            false,
            fetchedItems.value,
            database::addLiked,
            isDarkMode,
            context
        )
    }
}
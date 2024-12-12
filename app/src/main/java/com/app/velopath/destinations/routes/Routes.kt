package com.app.velopath.destinations.routes

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.velopath.database.FirebaseManagement
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
            AnimatedExpandableList(false, routeItems, database::addLiked, isDarkMode, context)
        }
    }
}
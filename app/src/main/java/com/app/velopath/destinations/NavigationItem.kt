package com.app.velopath.destinations

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat.getString
import com.app.velopath.R
import com.app.velopath.destinations.routes.Routes

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val destinationNav: Any
)

object GlobalData {
    fun getTabs(context: Context): List<NavigationItem> {
        return listOf(
            NavigationItem(
                title = getString(context, R.string.home),
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                destinationNav = Home
            ),
            NavigationItem(
                title = getString(context, R.string.your_profile),
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                destinationNav = Profile
            ),
            NavigationItem(
                title = getString(context, R.string.saved_routes),
                selectedIcon = Icons.Filled.Place,
                unselectedIcon = Icons.Outlined.Place,
                destinationNav = Routes
            ),
            NavigationItem(
                title = getString(context, R.string.about),
                selectedIcon = Icons.Filled.Info,
                unselectedIcon = Icons.Outlined.Info,
                destinationNav = Info
            ),
            NavigationItem(
                title = getString(context, R.string.settings),
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                destinationNav = Settings
            ),
            NavigationItem(
                title = getString(context, R.string.send_feedback),
                selectedIcon = Icons.Filled.ThumbUp,
                unselectedIcon = Icons.Outlined.ThumbUp,
                destinationNav = Feedback
            )
        )
    }
}
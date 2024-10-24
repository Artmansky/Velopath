package com.example.velopath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.velopath.destinations.Feedback
import com.example.velopath.destinations.GlobalData
import com.example.velopath.destinations.Home
import com.example.velopath.destinations.Info
import com.example.velopath.destinations.NavigationItem
import com.example.velopath.destinations.PrintFeedback
import com.example.velopath.destinations.PrintHome
import com.example.velopath.destinations.PrintInfo
import com.example.velopath.destinations.PrintProfile
import com.example.velopath.destinations.PrintRoutes
import com.example.velopath.destinations.PrintSettings
import com.example.velopath.destinations.Profile
import com.example.velopath.destinations.Routes
import com.example.velopath.destinations.Settings
import com.example.velopath.ui.theme.VelopathTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VelopathTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigator()
                }
            }
        }
    }
}

@Composable
fun MainNavigator() {
    val items = GlobalData.tabs
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }
    val navController = rememberNavController()

    ModalNavigationDrawer(
        drawerContent = {
            DrawerContent(
                items = items,
                selectedItemIndex = selectedItemIndex,
                onItemSelected = { index ->
                    selectedItemIndex = index
                    scope.launch { drawerState.close() }
                    navController.navigate(items[index].destinationNav) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                    }
                }
            )
        },
        drawerState = drawerState
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (selectedItemIndex) {
                0 -> {
                    IconButton(
                        onClick = {
                            scope.launch { drawerState.open() }
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                else -> {
                    TopBar(title = items[selectedItemIndex].title, onDrawerClick = {
                        scope.launch { drawerState.open() }
                    })
                }
            }
            NavHost(navController = navController, startDestination = Home) {
                composable<Home> { PrintHome() }
                composable<Profile> { PrintProfile() }
                composable<Routes> { PrintRoutes() }
                composable<Settings> { PrintSettings() }
                composable<Info> { PrintInfo() }
                composable<Feedback> { PrintFeedback() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, onDrawerClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = title)
        },
        navigationIcon = {
            IconButton(onClick = onDrawerClick) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Drawer")
            }
        }
    )
}

@Composable
fun DrawerContent(
    items: List<NavigationItem>,
    selectedItemIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    ModalDrawerSheet(modifier = Modifier.width(250.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        items.forEachIndexed { index, item ->
            NavigationDrawerItem(
                label = { Text(text = item.title) },
                selected = index == selectedItemIndex,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (index == selectedItemIndex) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}
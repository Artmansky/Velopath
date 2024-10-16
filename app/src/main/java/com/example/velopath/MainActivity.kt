package com.example.velopath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                    MainScreen()
                }
            }
        }
    }
}

@Preview
@Composable
fun MainScreen() {
    val items = listOf(
        NavigationItem(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
        ),
        NavigationItem(
            title = "Your Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
        ),
        NavigationItem(
            title = "Your Routes",
            selectedIcon = Icons.Filled.Place,
            unselectedIcon = Icons.Outlined.Place,
        ),
        NavigationItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
        ),
        NavigationItem(
            title = "App info",
            selectedIcon = Icons.Filled.Info,
            unselectedIcon = Icons.Outlined.Info,
        ),
        NavigationItem(
            title = "Send Feedback",
            selectedIcon = Icons.Filled.ThumbUp,
            unselectedIcon = Icons.Outlined.ThumbUp,
        ),
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }

    ModalNavigationDrawer(
        drawerContent = {
            DrawerContent(
                items = items,
                selectedItemIndex = selectedItemIndex,
                onItemSelected = { index ->
                    selectedItemIndex = index
                    scope.launch { drawerState.close() }
                }
            )
        },
        drawerState = drawerState
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = {
                    scope.launch { drawerState.open() }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
            }
        }
        Greeting(name = "Android")
    }
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Hello $name!",
            textAlign = TextAlign.Center
        )
    }
}
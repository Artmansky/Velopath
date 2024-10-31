package com.artmansky.velopath

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.artmansky.velopath.destinations.Feedback
import com.artmansky.velopath.destinations.GlobalData
import com.artmansky.velopath.destinations.Home
import com.artmansky.velopath.destinations.Info
import com.artmansky.velopath.destinations.PrintFeedback
import com.artmansky.velopath.destinations.PrintHome
import com.artmansky.velopath.destinations.PrintInfo
import com.artmansky.velopath.destinations.PrintProfile
import com.artmansky.velopath.destinations.PrintRoutes
import com.artmansky.velopath.destinations.PrintSettings
import com.artmansky.velopath.destinations.Profile
import com.artmansky.velopath.destinations.Routes
import com.artmansky.velopath.destinations.Settings
import com.artmansky.velopath.login.GoogleAuthUiClient
import com.artmansky.velopath.login.PrintSignInScreen
import com.artmansky.velopath.login.SignInScreen
import com.artmansky.velopath.login.SignInViewModel
import com.artmansky.velopath.ui.theme.VelopathTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VelopathTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                    }
                                },
                                userData = googleAuthUiClient.getSignedInUser()
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
                            NavHost(
                                navController = navController,
                                startDestination = SignInScreen
                            ) {
                                composable<SignInScreen> {
                                    val viewModel = viewModel<SignInViewModel>()
                                    val state by viewModel.state.collectAsStateWithLifecycle()

                                    LaunchedEffect(key1 = Unit) {
                                        if (googleAuthUiClient.getSignedInUser() != null) {
                                            navController.navigate(Home)
                                        }
                                    }

                                    val launcher = rememberLauncherForActivityResult(
                                        contract = ActivityResultContracts.StartIntentSenderForResult(),
                                        onResult = { result ->
                                            if (result.resultCode == RESULT_OK) {
                                                lifecycleScope.launch {
                                                    val signInResult =
                                                        googleAuthUiClient.signInWithIntent(
                                                            intent = result.data ?: return@launch
                                                        )
                                                    viewModel.onSignInResult(signInResult)
                                                }
                                            }
                                        }
                                    )

                                    LaunchedEffect(key1 = state.isSignInSuccessful) {
                                        if (state.isSignInSuccessful) {
                                            Toast.makeText(
                                                applicationContext,
                                                "Sign in successful",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            navController.navigate(Home)
                                            viewModel.resetState()
                                        }
                                    }

                                    PrintSignInScreen(
                                        state = state,
                                        onSignInClick = {
                                            lifecycleScope.launch {
                                                val signInIntentSender = googleAuthUiClient.signIn()
                                                launcher.launch(
                                                    IntentSenderRequest.Builder(
                                                        signInIntentSender ?: return@launch
                                                    ).build()
                                                )
                                            }
                                        }
                                    )
                                }
                                composable<Home> { PrintHome() }
                                composable<Profile> { PrintProfile() }
                                composable<Routes> { PrintRoutes() }
                                composable<Info> { PrintInfo() }
                                composable<Settings> { PrintSettings() }
                                composable<Feedback> { PrintFeedback() }
                            }
                        }
                    }
                }
            }
        }
    }
}
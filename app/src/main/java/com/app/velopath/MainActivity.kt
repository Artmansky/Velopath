package com.app.velopath

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.velopath.destinations.Feedback
import com.app.velopath.destinations.GlobalData
import com.app.velopath.destinations.Home
import com.app.velopath.destinations.Info
import com.app.velopath.destinations.PrintFeedback
import com.app.velopath.destinations.PrintHome
import com.app.velopath.destinations.PrintInfo
import com.app.velopath.destinations.PrintProfile
import com.app.velopath.destinations.PrintRoutes
import com.app.velopath.destinations.PrintSettings
import com.app.velopath.destinations.Profile
import com.app.velopath.destinations.Routes
import com.app.velopath.destinations.Settings
import com.app.velopath.login.PrintSignInScreen
import com.app.velopath.login.SignInScreen
import com.app.velopath.ui.theme.VelopathTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    private val currentUser: StateFlow<FirebaseUser?> = _currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            VelopathTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val items = GlobalData.tabs
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val navController = rememberNavController()
                    val credentialManager = CredentialManager.create(context)
                    val startDestination = if (auth.currentUser == null) SignInScreen else Home
                    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }
                    val gestures = rememberSaveable { mutableStateOf(false) }

                    auth.addAuthStateListener { firebaseAuth ->
                        _currentUser.value = firebaseAuth.currentUser
                    }
                    val user by currentUser.collectAsState()

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
                                userData = user
                            )
                        },
                        drawerState = drawerState,
                        gesturesEnabled = gestures.value
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            NavHost(
                                navController = navController,
                                startDestination = startDestination
                            ) {
                                composable<SignInScreen> {
                                    PrintSignInScreen(
                                        onSignInClick = {
                                            val googleIdOption = GetGoogleIdOption.Builder()
                                                .setFilterByAuthorizedAccounts(false)
                                                .setServerClientId(getString(R.string.default_web_client_id))
                                                .setAutoSelectEnabled(true)
                                                .build()

                                            val request = GetCredentialRequest.Builder()
                                                .addCredentialOption(googleIdOption)
                                                .build()

                                            scope.launch {
                                                try {
                                                    val result =
                                                        credentialManager.getCredential(
                                                            context = context,
                                                            request = request
                                                        )
                                                    val credential = result.credential
                                                    val googleIdTokenCredential =
                                                        GoogleIdTokenCredential
                                                            .createFrom(credential.data)
                                                    val googleIdToken =
                                                        googleIdTokenCredential.idToken

                                                    val firebaseCredential =
                                                        GoogleAuthProvider.getCredential(
                                                            googleIdToken,
                                                            null
                                                        )
                                                    Toast.makeText(
                                                        context,
                                                        "Logging You in, please wait",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    auth.signInWithCredential(firebaseCredential)
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                navController.navigate(Home)
                                                                gestures.value = true
                                                            }
                                                        }
                                                } catch (e: Exception) {
                                                    Toast.makeText(
                                                        context,
                                                        "Error: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    )
                                }
                                composable<Home> {
                                    gestures.value = true
                                    PrintHome(navButton = {
                                        IconButton(
                                            onClick = {
                                                scope.launch { drawerState.open() }
                                            },
                                            modifier = Modifier
                                                .size(56.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "Menu",
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    })
                                }
                                composable<Profile> {
                                    PrintProfile(
                                        navButton = {
                                            TopBar(
                                                title = items[selectedItemIndex].title,
                                                onDrawerClick = {
                                                    scope.launch { drawerState.open() }
                                                })
                                        },
                                        onSignOutClick = {
                                            auth.signOut()
                                            scope.launch {
                                                credentialManager.clearCredentialState(
                                                    ClearCredentialStateRequest()
                                                )
                                            }
                                            navController.navigate(SignInScreen)
                                            gestures.value = false
                                        }
                                    )
                                }
                                composable<Routes> {
                                    PrintRoutes(navButton = {
                                        TopBar(
                                            title = items[selectedItemIndex].title,
                                            onDrawerClick = {
                                                scope.launch { drawerState.open() }
                                            })
                                    })
                                }
                                composable<Info> {
                                    PrintInfo(navButton = {
                                        TopBar(
                                            title = items[selectedItemIndex].title,
                                            onDrawerClick = {
                                                scope.launch { drawerState.open() }
                                            })
                                    })
                                }
                                composable<Settings> {
                                    PrintSettings(navButton = {
                                        TopBar(
                                            title = items[selectedItemIndex].title,
                                            onDrawerClick = {
                                                scope.launch { drawerState.open() }
                                            })
                                    })
                                }
                                composable<Feedback> {
                                    PrintFeedback(navButton = {
                                        TopBar(
                                            title = items[selectedItemIndex].title,
                                            onDrawerClick = {
                                                scope.launch { drawerState.open() }
                                            })
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
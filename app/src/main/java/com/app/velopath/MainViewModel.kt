package com.app.velopath

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Switch
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {
    val auth: FirebaseAuth = Firebase.auth
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _darkTheme = MutableStateFlow(false)
    val darkTheme: StateFlow<Boolean> = _darkTheme

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            auth.addAuthStateListener { firebaseAuth ->
                _currentUser.value = firebaseAuth.currentUser
            }

            preferencesManager.darkThemeFlow.collect { isDark ->
                _darkTheme.value = isDark
            }

            _isReady.value = true
        }
    }

    fun saveTheme(isDark: Boolean) {
        viewModelScope.launch {
            preferencesManager.saveDarkThemePreference(isDark)
        }
    }
}


@Composable
fun MainNavigation(
    user: FirebaseUser?,
    auth: FirebaseAuth,
    clientID: String,
    database: FirebaseManagement,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val items = GlobalData.tabs
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navController = rememberNavController()
    val credentialManager = CredentialManager.create(context)
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
    val gestures = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    //Maybe move some funtions to separate blocks of code??

    ModalNavigationDrawer(
        drawerContent = {
            DrawerContent(
                items = items,
                selectedItemIndex = selectedItemIndex,
                onItemSelected = { index ->
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
                startDestination = if (auth.currentUser == null) SignInScreen else Home
            ) {
                composable<SignInScreen> {
                    gestures.value = false
                    PrintSignInScreen(
                        onSignInClick = {
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(clientID)
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
                                                selectedItemIndex = 0
                                                navController.navigate(Profile)
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
                        },
                        darkTheme = darkTheme
                    )
                }
                composable<Home> {
                    selectedItemIndex = 0
                    gestures.value = false
                    PrintHome(navButton = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    scope.launch { drawerState.open() }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    })
                }
                composable<Profile> {
                    selectedItemIndex = 1
                    gestures.value = true
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
                            navController.popBackStack()
                            navController.navigate(SignInScreen)
                        },
                        userData = user
                    )
                }
                composable<Routes> {
                    selectedItemIndex = 2
                    gestures.value = true
                    PrintRoutes(navButton = {
                        TopBar(
                            title = items[selectedItemIndex].title,
                            onDrawerClick = {
                                scope.launch { drawerState.open() }
                            })
                    })
                }
                composable<Info> {
                    selectedItemIndex = 3
                    gestures.value = true
                    PrintInfo(navButton = {
                        TopBar(
                            title = items[selectedItemIndex].title,
                            onDrawerClick = {
                                scope.launch { drawerState.open() }
                            })
                    })
                }
                composable<Settings> {
                    selectedItemIndex = 4
                    gestures.value = true
                    PrintSettings(
                        navButton = {
                            TopBar(
                                title = items[selectedItemIndex].title,
                                onDrawerClick = {
                                    scope.launch { drawerState.open() }
                                })
                        },
                        context = context,
                        toggleFunction = {
                            Switch(
                                checked = darkTheme,
                                onCheckedChange = {
                                    onDarkThemeChange(!darkTheme)
                                }
                            )
                        },
                    )
                }
                composable<Feedback> {
                    selectedItemIndex = 5
                    gestures.value = true
                    PrintFeedback(
                        navButton = {
                            TopBar(
                                title = items[selectedItemIndex].title,
                                onDrawerClick = {
                                    scope.launch { drawerState.open() }
                                })
                        },
                        onClick = database::addFeedbackMessage
                    )
                }
            }
        }
    }
}
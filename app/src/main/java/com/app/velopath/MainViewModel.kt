package com.app.velopath

import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
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
import com.app.velopath.destinations.PrintSettings
import com.app.velopath.destinations.Profile
import com.app.velopath.destinations.Settings
import com.app.velopath.destinations.routes.PrintRoutes
import com.app.velopath.destinations.routes.Routes
import com.app.velopath.handlers.isNetworkAvailable
import com.app.velopath.login.PrintSignInScreen
import com.app.velopath.login.SignInScreen
import com.app.velopath.mapsHandling.MapsHandling
import com.app.velopath.ui.DrawerContent
import com.app.velopath.ui.PreferencesManager
import com.app.velopath.ui.theme.ThemeMode
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {
    val auth: FirebaseAuth = Firebase.auth
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _darkTheme = MutableStateFlow(ThemeMode.SYSTEM_DEFAULT)
    val darkTheme: StateFlow<ThemeMode> = _darkTheme.asStateFlow()

    init {
        viewModelScope.launch {
            auth.addAuthStateListener { firebaseAuth ->
                _currentUser.value = firebaseAuth.currentUser
            }
            preferencesManager.themeMode.collect { themeMode ->
                _darkTheme.value = themeMode
            }
        }
    }

    fun saveTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.saveThemePreference(themeMode)
        }
    }

    fun logoutUser(
        scope: CoroutineScope,
        credentialManager: CredentialManager,
        navController: NavController
    ) {
        auth.signOut()
        scope.launch {
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
        }
        navController.popBackStack()
        navController.navigate(SignInScreen)
    }
}


@Composable
fun MainNavigation(
    user: FirebaseUser?,
    viewModel: MainViewModel,
    clientID: String,
    database: FirebaseManagement,
    themeMode: ThemeMode,
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

    val mapsHandler = MapsHandling(context)

    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }

    ModalNavigationDrawer(
        drawerContent = {
            DrawerContent(
                items = items,
                selectedItemIndex = selectedItemIndex,
                onItemSelected = { index ->
                    scope.launch { drawerState.close() }
                    if (index != selectedItemIndex) {
                        navController.navigate(items[index].destinationNav) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    }
                },
                userName = user?.displayName ?: "Nickname"
            )
        },
        drawerState = drawerState,
        gesturesEnabled = gestures.value
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = if (viewModel.auth.currentUser == null) SignInScreen else Home
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
                                    viewModel.auth.signInWithCredential(firebaseCredential)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                selectedItemIndex = 0
                                                navController.navigate(Home)
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
                        darkTheme = isDarkTheme
                    )
                }
                composable<Home> {
                    selectedItemIndex = 0
                    gestures.value = false
                    PrintHome(
                        mapsHandler = mapsHandler,
                        darkMode = isDarkTheme,
                        onClick = {
                            scope.launch { drawerState.open() }
                        })
                }
                composable<Profile> {
                    selectedItemIndex = 1
                    gestures.value = true
                    PrintProfile(
                        title = items[selectedItemIndex].title,
                        onClick = {
                            scope.launch { drawerState.open() }
                        },
                        userData = user,
                        isDarkMode = isDarkTheme,
                        context = context
                    )
                }
                composable<Routes> {
                    selectedItemIndex = 2
                    gestures.value = true
                    PrintRoutes(
                        title = items[selectedItemIndex].title,
                        onClick = {
                            scope.launch { drawerState.open() }
                        },
                        isDarkMode = isDarkTheme,
                        context = context
                    )
                }
                composable<Info> {
                    selectedItemIndex = 3
                    gestures.value = true
                    PrintInfo(title = items[selectedItemIndex].title,
                        onClick = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }
                composable<Settings> {
                    selectedItemIndex = 4
                    gestures.value = true
                    PrintSettings(
                        title = items[selectedItemIndex].title,
                        onClick = {
                            scope.launch { drawerState.open() }
                        },
                        context = context,
                        toggleFunction = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { isDark ->
                                    onDarkThemeChange(isDark)
                                }
                            )
                        },
                        onSignOut = {
                            viewModel.logoutUser(scope, credentialManager, navController)
                        }
                    )
                }
                composable<Feedback> {
                    selectedItemIndex = 5
                    gestures.value = true
                    PrintFeedback(
                        title = items[selectedItemIndex].title,
                        onClick = {
                            scope.launch { drawerState.open() }
                        },
                        onFeedbackClick = database::addFeedbackMessage,
                        networkAvailable = ::isNetworkAvailable
                    )
                }
            }
        }
    }
}
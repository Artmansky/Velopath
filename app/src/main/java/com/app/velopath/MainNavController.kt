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
import kotlinx.coroutines.launch

@Composable
fun MainNavigation(user: FirebaseUser?, auth: FirebaseAuth, clientID: String) {
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
                        }
                    )
                }
                composable<Home> {
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
                    gestures.value = true
                    PrintSettings(navButton = {
                        TopBar(
                            title = items[selectedItemIndex].title,
                            onDrawerClick = {
                                scope.launch { drawerState.open() }
                            })
                    })
                }
                composable<Feedback> {
                    gestures.value = true
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